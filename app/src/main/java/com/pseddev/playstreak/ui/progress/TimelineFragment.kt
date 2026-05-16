/*
 * Timeline Fragment Functionality Temporarily Disabled
 *
 * DevCycle 2025-0005 Phase 4: Timeline fragment functionality has been disabled
 * since the Timeline tab has been removed from navigation for evaluation.
 *
 * RESTORATION PROCESS:
 * 1. Uncomment the onViewCreated() method calls (lines 52-56)
 * 2. Remove the temporary disable comments
 * 3. Restore Timeline tab in ViewProgressFragment.kt
 *
 * All Timeline functionality code is preserved intact for quick restoration.
 */
package com.pseddev.mystreak.ui.progress

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import com.pseddev.mystreak.MyStreakApplication
import com.pseddev.mystreak.R
import com.pseddev.mystreak.databinding.FragmentTimelineBinding
import com.pseddev.mystreak.ui.progress.EditActivityStorage
import java.text.SimpleDateFormat
import java.util.*

class TimelineFragment : Fragment() {

    private var _binding: FragmentTimelineBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TimelineViewModel by viewModels {
        TimelineViewModelFactory(
            (requireActivity().application as MyStreakApplication).repository,
            requireContext()
        )
    }

    private lateinit var adapter: TimelineAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimelineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Timeline functionality temporarily disabled for evaluation
        // Timeline tab has been removed from navigation - this fragment should not be created
        // Keeping code intact for easy restoration if needed

        /*
        // Timeline functionality - temporarily disabled for evaluation
        setupRecyclerView()
        setupFilterUI()
        observeViewModel()
        */
    }

    private fun setupRecyclerView() {
        adapter = TimelineAdapter(
            onDeleteClick = { activityWithPiece ->
                showDeleteConfirmationDialog(activityWithPiece)
            },
            onEditClick = { activityWithPiece ->
                editActivity(activityWithPiece)
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // Enable nested scrolling for proper interaction with ViewPager2
        binding.recyclerView.isNestedScrollingEnabled = true
    }

    private fun setupFilterUI() {
        // Feature #33A: Show filter UI only for Pro users
        if (viewModel.isProUser) {
            binding.filterSection.visibility = View.VISIBLE
            binding.filterDivider.visibility = View.VISIBLE

            // Feature #33B-Safe: Set up simple switch listener
            binding.performanceFilterSwitch.setOnCheckedChangeListener { _, isChecked ->
                android.util.Log.d("TimelineFragment", "Feature #33B-Safe: Switch toggled to $isChecked")
                if (isChecked != (viewModel.showPerformancesOnly.value ?: false)) {
                    viewModel.toggleFilter()
                }
            }
        } else {
            binding.filterSection.visibility = View.GONE
            binding.filterDivider.visibility = View.GONE
        }
    }

    private fun observeViewModel() {
        viewModel.activitiesWithPieces.observe(viewLifecycleOwner) { activities ->
            updateEmptyView(activities)
            adapter.submitList(activities)
        }

        // Feature #33B-Safe: Observe filter state changes
        if (viewModel.isProUser) {
            viewModel.showPerformancesOnly.observe(viewLifecycleOwner) { showPerformancesOnly ->
                android.util.Log.d("TimelineFragment", "Feature #33B-Safe: Filter state changed to $showPerformancesOnly")
                // Update switch to match ViewModel state (prevent listener loops)
                if (binding.performanceFilterSwitch.isChecked != showPerformancesOnly) {
                    binding.performanceFilterSwitch.isChecked = showPerformancesOnly
                }
            }
        }
    }

    private fun updateEmptyView(activities: List<ActivityWithPiece>) {
        if (activities.isEmpty()) {
            binding.emptyView.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE

            // Feature #33C: Update empty message based on filter state
            val emptyMessage = if (viewModel.isProUser && (viewModel.showPerformancesOnly.value == true)) {
                "No performance activities recorded yet"
            } else {
                "No activities recorded yet"
            }
            binding.emptyView.text = emptyMessage
        } else {
            binding.emptyView.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }

    private fun editActivity(activityWithPiece: ActivityWithPiece) {
        navigateToEditActivity(activityWithPiece)
    }

    private fun showDeleteConfirmationDialog(activityWithPiece: ActivityWithPiece) {
        val dateFormat = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US)
        val dateString = dateFormat.format(Date(activityWithPiece.activity.timestamp))
        val pieceName = activityWithPiece.pieceOrTechnique.name

        AlertDialog.Builder(requireContext())
            .setTitle("Delete Activity")
            .setMessage("Are you sure you want to delete this activity?\n\n$pieceName\n$dateString")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteActivity(activityWithPiece)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
