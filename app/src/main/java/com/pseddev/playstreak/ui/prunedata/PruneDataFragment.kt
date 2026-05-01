package com.pseddev.mystreak.ui.prunedata

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.pseddev.mystreak.MyStreakApplication
import com.pseddev.mystreak.R
import com.pseddev.mystreak.databinding.FragmentPruneDataBinding
import kotlin.math.min

class PruneDataFragment : Fragment() {

    private var _binding: FragmentPruneDataBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PruneDataViewModel by viewModels {
        PruneDataViewModelFactory(
            (requireActivity().application as MyStreakApplication).repository,
            requireContext()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPruneDataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.activityCounts.observe(viewLifecycleOwner) { counts ->
            binding.activitiesStoredText.text = "Activities Stored: ${counts.stored}"
            binding.maximumActivitiesText.text = "Maximum Activities: ${counts.maximum}"
            binding.lifetimeActivitiesText.text = "Lifetime Activities: ${counts.lifetime}"

            // Update button text to show actual count to be pruned
            val countToPrune = min(100, counts.stored)
            binding.pruneOldestActivitiesButton.text = if (counts.stored > 0) {
                "Prune Oldest $countToPrune Activities"
            } else {
                "No Activities to Prune"
            }

            // Disable button if no activities
            binding.pruneOldestActivitiesButton.isEnabled = counts.stored > 0
        }

        viewModel.pruningInProgress.observe(viewLifecycleOwner) { inProgress ->
            val counts = viewModel.activityCounts.value ?: ActivityCounts(0, 0, 0)
            binding.pruneOldestActivitiesButton.isEnabled = !inProgress && counts.stored > 0
            binding.pruneOldestActivitiesButton.text = if (inProgress) {
                "Pruning Activities..."
            } else {
                val countToPrune = min(100, counts.stored)
                if (counts.stored > 0) "Prune Oldest $countToPrune Activities" else "No Activities to Prune"
            }
        }

        viewModel.pruningResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                when (it) {
                    is PruningResult.Success -> {
                        showResultDialog(
                            "Pruning Complete",
                            "Successfully deleted ${it.deletedCount} oldest activities.\n\nAll pieces, statistics, and favorites have been preserved."
                        )
                    }
                    is PruningResult.NoActivitiesToPrune -> {
                        showResultDialog(
                            "No Activities to Prune",
                            "There are no activities in the database to delete."
                        )
                    }
                    is PruningResult.Error -> {
                        showResultDialog(
                            "Pruning Failed",
                            "An error occurred during pruning: ${it.message}\n\nNo data has been deleted."
                        )
                    }
                }
                viewModel.clearPruningResult()
            }
        }
    }

    private fun setupClickListeners() {
        binding.exportDataFirstButton.setOnClickListener {
            // Navigate to Import/Export page
            findNavController().navigate(R.id.action_pruneDataFragment_to_importExportFragment)
        }

        binding.pruneOldestActivitiesButton.setOnClickListener {
            val counts = viewModel.activityCounts.value ?: ActivityCounts(0, 0, 0)
            if (counts.stored > 0) {
                showPruningConfirmationDialog(counts.stored)
            }
        }
    }

    private fun showPruningConfirmationDialog(activityCount: Int) {
        val countToPrune = min(100, activityCount)

        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Activity Pruning")
            .setMessage("This will permanently delete the oldest $countToPrune activities.\n\nPiece statistics and piece records will remain unchanged.\n\nThis action cannot be undone.\n\nConsider exporting your data first as a safety backup.")
            .setPositiveButton("Prune Activities") { _, _ ->
                viewModel.pruneOldestActivities()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showResultDialog(title: String, message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}