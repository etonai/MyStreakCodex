package com.pseddev.mystreak.ui.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.app.AlertDialog
import android.widget.Toast
import com.pseddev.mystreak.MyStreakApplication
import com.pseddev.mystreak.R
import com.pseddev.mystreak.databinding.FragmentPiecesBinding
import com.pseddev.mystreak.ui.progress.QuickAddActivityDialogFragment
import com.pseddev.mystreak.utils.ProUserManager
import com.pseddev.mystreak.utils.DateFormatter

class PiecesFragment : Fragment() {

    private var _binding: FragmentPiecesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PiecesViewModel by viewModels {
        PiecesViewModelFactory(
            (requireActivity().application as MyStreakApplication).repository,
            requireContext()
        )
    }

    private lateinit var adapter: PiecesAdapter
    private var shouldScrollToTop = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPiecesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        setupSortingControls()

        val proUserManager = ProUserManager.getInstance(requireContext())

        adapter = PiecesAdapter(
            onPieceClick = { pieceWithStats ->
                viewModel.selectPiece(pieceWithStats.piece.id)
            },
            onFavoriteToggle = { pieceWithStats ->
                val success = viewModel.toggleFavorite(pieceWithStats)
                if (!success) {
                    showFavoriteLimitPrompt()
                }
            },
            onAddActivityClick = { pieceWithStats ->
                // Show quick add activity dialog with piece pre-filled
                val dialog = QuickAddActivityDialogFragment.newInstance(
                    pieceWithStats.piece.id,
                    pieceWithStats.piece.name,
                    "dashboard_quick"
                )
                dialog.show(parentFragmentManager, "QuickAddActivityDialog")
            },
            onEditClick = { pieceWithStats ->
                val dialog = EditPieceDialogFragment.newInstance(
                    pieceWithStats.piece.id,
                    pieceWithStats.piece.name,
                    pieceWithStats.piece.type,
                    pieceWithStats.piece.color,
                    pieceWithStats.piece.priority,
                    pieceWithStats.piece.minimumSuccess,
                    pieceWithStats.piece.mediumSuccess,
                    pieceWithStats.piece.highSuccess,
                    pieceWithStats.piece.isActive
                )
                dialog.show(parentFragmentManager, "EditPieceDialog")
            },
            onDeleteClick = { pieceWithStats ->
                showDeleteConfirmationDialog(pieceWithStats)
            },
            proUserManager
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.piecesWithStats.observe(viewLifecycleOwner) { pieces ->
            if (pieces.isEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.emptyView.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                adapter.submitList(pieces) {
                    if (shouldScrollToTop) {
                        binding.recyclerView.itemAnimator?.endAnimations()
                        binding.recyclerView.scrollToPosition(0)
                        shouldScrollToTop = false
                    }
                }
            }
        }

        viewModel.selectedPieceDetails.observe(viewLifecycleOwner) { details ->
            details?.let {
                showPieceDetails(it)
            }
        }
    }

    private fun showPieceDetails(details: PieceDetails) {
        binding.pieceDetailsCard.visibility = View.VISIBLE
        val piece = details.piece

        // Header
        binding.pieceNameText.text = piece.name

        // Basic Information Section
        binding.pieceTypeText.text = "Status: Active"
        binding.pieceTypeText.text = "Status: ${if (piece.isActive) "Active" else "Inactive"}"
        binding.isFavoriteText.text = "Priority: ${piece.priority.name.lowercase().replaceFirstChar { it.uppercase() }}"
        binding.dateCreatedText.text = "Created: ${DateFormatter.formatDateOnly(piece.dateCreated)}"

        binding.practiceCountText.text = "Total Activities: ${details.activities.size}"
        binding.lastPracticeText.text = "Last Activity: ${DateFormatter.formatDate(details.lastActivity?.timestamp)}"
        binding.secondLastPracticeText.text = "Minimum Success: ${piece.minimumSuccess}"
        binding.thirdLastPracticeText.text = "Medium Success: ${piece.mediumSuccess}"
        binding.lastSatisfactoryPracticeText.text = "High Success: ${piece.highSuccess}"

        binding.performanceCountText.text = "Task Color: ${piece.color}"
        binding.lastPerformanceText.text = ""
        binding.secondLastPerformanceText.text = ""
        binding.thirdLastPerformanceText.text = ""
        binding.lastSatisfactoryPerformanceText.text = ""

        // Legacy Activity Data Section (calculated from activities for comparison)
        val practiceCountLegacy = details.activities.count { it.activityType == com.pseddev.mystreak.data.entities.ActivityType.PRACTICE }
        val performanceCountLegacy = details.activities.count { it.activityType == com.pseddev.mystreak.data.entities.ActivityType.PERFORMANCE }
        binding.totalActivitiesText.text = "Total Activities: ${details.activities.size}"

        val totalMinutes = details.activities.filter { it.minutes > 0 }.sumOf { it.minutes }
        if (totalMinutes > 0) {
            binding.totalTimeText.text = "Legacy tracked time: $totalMinutes minutes"
            binding.totalTimeText.visibility = View.VISIBLE
        } else {
            binding.totalTimeText.text = "Legacy tracked time: No time data"
            binding.totalTimeText.visibility = View.VISIBLE
        }

        if (details.lastActivity != null) {
            binding.lastActivityText.text = "Last Activity: ${DateFormatter.formatDate(details.lastActivity.timestamp)}"
        } else {
            binding.lastActivityText.text = "Last Activity: No activities recorded"
        }

        // System Information Section
        binding.lastUpdatedText.text = "Last Updated: ${DateFormatter.formatDateTime(piece.lastUpdated)}"

        binding.closeDetailsButton.setOnClickListener {
            binding.pieceDetailsCard.visibility = View.GONE
            viewModel.clearSelection()
        }
    }

    private fun setupClickListeners() {
        binding.buttonAddPiece.setOnClickListener {
            findNavController().navigate(R.id.action_viewProgressFragment_to_addPieceFragment)
        }
    }

    private fun setupSortingControls() {
        // Set up chip selection listener
        binding.sortChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val sortType = when (checkedIds[0]) {
                    R.id.chipAlphabetical -> SortType.ALPHABETICAL
                    R.id.chipLastDate -> SortType.LAST_DATE
                    R.id.chipActivityCount -> SortType.ACTIVITY_COUNT
                    R.id.chipPriority -> SortType.PRIORITY
                    else -> SortType.ALPHABETICAL
                }
                viewModel.setSortType(sortType)
                updateSortDirectionButton()
                shouldScrollToTop = true
            }
        }

        // Set up sort direction button
        binding.buttonSortDirection.setOnClickListener {
            viewModel.toggleSortDirection()
            updateSortDirectionButton()
            shouldScrollToTop = true
        }

        // Initialize sort direction button
        updateSortDirectionButton()
    }

    private fun updateSortDirectionButton() {
        val direction = viewModel.getCurrentSortDirection()
        binding.buttonSortDirection.text = if (direction == SortDirection.ASCENDING) "↑" else "↓"
        binding.buttonSortDirection.isEnabled = true
        binding.buttonSortDirection.alpha = 1.0f
    }

    private fun showFavoriteLimitPrompt() {
        AlertDialog.Builder(requireContext())
            .setTitle("Favorite Limit")
            .setMessage("Priority limits will be replaced by MyStreak task priority in a later phase.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showDeleteConfirmationDialog(pieceWithStats: PieceWithStats) {
        val activityText = if (pieceWithStats.activityCount == 1) {
            "1 activity"
        } else {
            "${pieceWithStats.activityCount} activities"
        }

        val message = "Delete \"${pieceWithStats.piece.name}\" and all its $activityText?\n\n" +
                "This action cannot be undone."

        AlertDialog.Builder(requireContext())
            .setTitle("Delete Task")
            .setMessage(message)
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deletePiece(pieceWithStats)
                Toast.makeText(requireContext(), "Task deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
