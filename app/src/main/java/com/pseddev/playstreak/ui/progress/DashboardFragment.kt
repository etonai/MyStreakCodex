package com.pseddev.playstreak.ui.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.pseddev.playstreak.PlayStreakApplication
import com.pseddev.playstreak.R
import com.pseddev.playstreak.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels {
        DashboardViewModelFactory(
            (requireActivity().application as PlayStreakApplication).repository,
            requireContext()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()

        viewModel.todayActivities.observe(viewLifecycleOwner) { activities ->
            binding.todayCountText.text = "${activities.size} activities"

            if (activities.isNotEmpty()) {
                binding.todayActivitiesGroup.visibility = View.VISIBLE
                binding.todayActivitiesList.text = activities.joinToString("\n") { activityWithPiece ->
                    val activity = activityWithPiece.activity
                    val task = activityWithPiece.pieceOrTechnique
                    val time = android.text.format.DateFormat.format("h:mm a", activity.timestamp)
                    val level = "(${activity.level})"
                    val minutes = if (activity.minutes > 0) " (${activity.minutes} min)" else ""
                    "- $time - ${task.name} - Success $level$minutes"
                }
            } else {
                binding.todayActivitiesGroup.visibility = View.GONE
                binding.todayActivitiesList.text = ""
            }
        }

        viewModel.yesterdayActivities.observe(viewLifecycleOwner) { activities ->
            binding.yesterdayCountText.text = "${activities.size} activities"

            if (activities.isNotEmpty()) {
                binding.yesterdayActivitiesGroup.visibility = View.VISIBLE
                binding.yesterdayActivitiesList.text = activities.joinToString("\n") { activityWithPiece ->
                    val activity = activityWithPiece.activity
                    val task = activityWithPiece.pieceOrTechnique
                    val time = android.text.format.DateFormat.format("h:mm a", activity.timestamp)
                    val level = "(${activity.level})"
                    val minutes = if (activity.minutes > 0) " (${activity.minutes} min)" else ""
                    "- $time - ${task.name} - Success $level$minutes"
                }
            } else {
                binding.yesterdayActivitiesGroup.visibility = View.GONE
                binding.yesterdayActivitiesList.text = ""
            }
        }

        viewModel.currentStreak.observe(viewLifecycleOwner) { streak ->
            binding.currentStreakText.text = "$streak day${if (streak != 1) "s" else ""}"
        }

        viewModel.weekSummary.observe(viewLifecycleOwner) { summary ->
            binding.weekSummaryText.text = summary
        }

        // Suggestions remain in the legacy model for now, but are not part of MyStreak Phase 1 UI.
        binding.suggestionsCard.visibility = View.GONE
        binding.performanceSuggestionsCard.visibility = View.GONE
    }

    private fun setupClickListeners() {
        binding.buttonAddActivity.setOnClickListener {
            findNavController().navigate(R.id.action_viewProgressFragment_to_addActivityFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
