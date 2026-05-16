package com.pseddev.mystreak.ui.progress

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.pseddev.mystreak.MyStreakApplication
import com.pseddev.mystreak.R
import com.pseddev.mystreak.data.entities.ActivityType
import com.pseddev.mystreak.databinding.FragmentDashboardBinding
import com.pseddev.mystreak.databinding.ItemDashboardActivityBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels {
        DashboardViewModelFactory(
            (requireActivity().application as MyStreakApplication).repository,
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
            binding.todayCountText.text = activityCountText(activities.size)
            bindActivityList(
                container = binding.todayActivitiesList,
                emptyView = binding.todayActivitiesEmptyText,
                activities = activities
            )
        }

        viewModel.yesterdayActivities.observe(viewLifecycleOwner) { activities ->
            binding.yesterdayCountText.text = activityCountText(activities.size)
            bindActivityList(
                container = binding.yesterdayActivitiesList,
                emptyView = binding.yesterdayActivitiesEmptyText,
                activities = activities
            )
        }

        viewModel.currentStreak.observe(viewLifecycleOwner) { streak ->
            binding.currentStreakText.text = "$streak day${if (streak != 1) "s" else ""}"
        }

        viewModel.weekSummary.observe(viewLifecycleOwner) { summary ->
            binding.weekSummaryText.text = summary
        }

        viewModel.highPriorityOutstanding.observe(viewLifecycleOwner) { taskNames ->
            binding.highPriorityOutstandingText.text = if (taskNames.isEmpty()) {
                "All high priority tasks are done for today."
            } else {
                taskNames.joinToString("\n") { "- $it" }
            }
        }

        // Suggestions remain in the legacy model for now, but are not part of MyStreak Phase 1 UI.
        binding.suggestionsCard.visibility = View.GONE
        binding.performanceSuggestionsCard.visibility = View.GONE
    }

    private fun setupClickListeners() {
        binding.buttonAddActivity.setOnClickListener {
            EditActivityStorage.clearEditState()
            findNavController().navigate(
                R.id.selectPieceFragment,
                bundleOf("activityType" to ActivityType.PRACTICE)
            )
        }
    }

    private fun bindActivityList(
        container: LinearLayout,
        emptyView: View,
        activities: List<ActivityWithPiece>
    ) {
        container.removeAllViews()
        emptyView.visibility = if (activities.isEmpty()) View.VISIBLE else View.GONE
        container.visibility = if (activities.isEmpty()) View.GONE else View.VISIBLE

        activities.forEach { item ->
            val itemBinding = ItemDashboardActivityBinding.inflate(
                layoutInflater,
                container,
                false
            )
            bindActivityRow(itemBinding, item)
            container.addView(itemBinding.root)
        }
    }

    private fun bindActivityRow(
        itemBinding: ItemDashboardActivityBinding,
        item: ActivityWithPiece
    ) {
        val activity = item.activity
        val task = item.pieceOrTechnique
        val time = SimpleDateFormat("h:mm a", Locale.US).format(Date(activity.timestamp))

        itemBinding.activityPrimaryText.text = "$time - ${task.name}"
        itemBinding.activitySecondaryText.text = activityDescription(activity, task)

        val indicator = itemBinding.taskColorIndicator.background.mutate() as? GradientDrawable
        indicator?.setColor(parseTaskColor(task.color))
        itemBinding.taskColorIndicator.background = indicator

        itemBinding.root.setOnClickListener {
            showNoteDetailDialog(item)
        }

        itemBinding.editButton.setOnClickListener {
            editActivity(item)
        }

        itemBinding.deleteButton.setOnClickListener {
            showDeleteConfirmationDialog(item)
        }
    }

    private fun activityCountText(count: Int): String {
        return "$count activit${if (count == 1) "y" else "ies"}"
    }

    private fun parseTaskColor(color: String): Int {
        return try {
            Color.parseColor(color)
        } catch (_: IllegalArgumentException) {
            Color.parseColor("#66B2FF")
        }
    }

    private fun editActivity(activityWithPiece: ActivityWithPiece) {
        navigateToEditActivity(activityWithPiece)
    }

    private fun showNoteDetailDialog(item: ActivityWithPiece) {
        if (!isAdded) return
        val activity = item.activity
        val task = item.pieceOrTechnique
        val dateFormat = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US)
        val dateString = dateFormat.format(Date(activity.timestamp))
        val successText = successLevelDescription(activity.successLevel, task)
        val notes = activity.notes.trim()
        val notesSection = if (notes.isEmpty()) "No notes" else notes

        AlertDialog.Builder(requireContext())
            .setTitle(task.name)
            .setMessage("$dateString\n$successText\n\n$notesSection")
            .setPositiveButton("Close", null)
            .show()
    }

    private fun showDeleteConfirmationDialog(activityWithPiece: ActivityWithPiece) {
        val dateFormat = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US)
        val dateString = dateFormat.format(Date(activityWithPiece.activity.timestamp))
        val taskName = activityWithPiece.pieceOrTechnique.name

        AlertDialog.Builder(requireContext())
            .setTitle("Delete Activity")
            .setMessage("Delete this activity?\n\n$taskName\n$dateString")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteActivity(activityWithPiece.activity)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshDateRanges()
    }
}
