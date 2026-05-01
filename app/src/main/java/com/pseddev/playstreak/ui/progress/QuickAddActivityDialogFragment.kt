package com.pseddev.mystreak.ui.progress

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import android.widget.ArrayAdapter
import android.widget.Toast
import com.pseddev.mystreak.MyStreakApplication
import com.pseddev.mystreak.R
import com.pseddev.mystreak.data.entities.Activity
import com.pseddev.mystreak.data.entities.ActivityType
import com.pseddev.mystreak.databinding.DialogQuickAddActivityBinding
import kotlinx.coroutines.launch
import java.util.*

class QuickAddActivityDialogFragment : DialogFragment() {

    private var _binding: DialogQuickAddActivityBinding? = null
    private val binding get() = _binding!!

    private val viewModel: QuickAddActivityViewModel by viewModels {
        QuickAddActivityViewModelFactory(
            (requireActivity().application as MyStreakApplication).repository,
            requireContext()
        )
    }

    private var pieceId: Long = -1
    private var pieceName: String = ""
    private var source: String = "dashboard_quick"

    companion object {
        private const val ARG_PIECE_ID = "piece_id"
        private const val ARG_PIECE_NAME = "piece_name"
        private const val ARG_SOURCE = "source"

        fun newInstance(pieceId: Long, pieceName: String, source: String = "dashboard_quick"): QuickAddActivityDialogFragment {
            val fragment = QuickAddActivityDialogFragment()
            val args = Bundle().apply {
                putLong(ARG_PIECE_ID, pieceId)
                putString(ARG_PIECE_NAME, pieceName)
                putString(ARG_SOURCE, source)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pieceId = it.getLong(ARG_PIECE_ID)
            pieceName = it.getString(ARG_PIECE_NAME, "")
            source = it.getString(ARG_SOURCE, "dashboard_quick")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogQuickAddActivityBinding.inflate(layoutInflater)

        setupViews()
        setupObservers()

        return AlertDialog.Builder(requireContext())
            .setTitle("Add Activity for $pieceName")
            .setView(binding.root)
            .setPositiveButton("Add Activity") { _, _ ->
                addActivity()
            }
            .setNegativeButton("Cancel", null)
            .create()
    }

    private fun setupViews() {
        binding.pieceNameText.text = pieceName

        viewModel.getTask(pieceId).observe(this) { task ->
            val levels = if (task != null) {
                listOf(
                    "Minimum - ${task.minimumSuccess}",
                    "Medium - ${task.mediumSuccess}",
                    "High - ${task.highSuccess}"
                )
            } else {
                listOf("Minimum", "Medium", "High")
            }

            val levelAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                levels
            )
            levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.levelSpinner.adapter = levelAdapter
        }
    }

    private fun setupObservers() {
        viewModel.addResult.observe(this) { result ->
            result.fold(
                onSuccess = {
                    Toast.makeText(context, "Activity added successfully!", Toast.LENGTH_SHORT).show()
                    dismiss()
                },
                onFailure = { exception ->
                    Toast.makeText(context, "Failed to add activity: ${exception.message}", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun addActivity() {
        val level = binding.levelSpinner.selectedItemPosition + 1

        val activity = Activity(
            id = 0, // Will be auto-generated
            pieceOrTechniqueId = pieceId,
            activityType = ActivityType.PRACTICE,
            timestamp = System.currentTimeMillis(),
            level = level,
            minutes = -1,
            notes = "",
            performanceType = "activity"
        )

        viewModel.addActivity(activity, source)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
