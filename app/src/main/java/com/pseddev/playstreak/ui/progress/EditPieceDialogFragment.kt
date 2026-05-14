package com.pseddev.mystreak.ui.progress

import android.app.Dialog
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.pseddev.mystreak.MyStreakApplication
import com.pseddev.mystreak.data.entities.ItemType
import com.pseddev.mystreak.data.entities.TaskKind
import com.pseddev.mystreak.data.entities.TaskPriority
import com.pseddev.mystreak.databinding.DialogEditPieceBinding

class EditPieceDialogFragment : DialogFragment() {

    private var _binding: DialogEditPieceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PiecesViewModel by viewModels({requireParentFragment()}) {
        PiecesViewModelFactory(
            (requireActivity().application as MyStreakApplication).repository,
            requireContext()
        )
    }

    private var pieceId: Long = -1
    private var currentName: String = ""
    private var currentColor: String = "#66B2FF"
    private var currentPriority: TaskPriority = TaskPriority.LOW
    private var currentTaskKind: TaskKind = TaskKind.STANDARD
    private var currentMinimumSuccess: String = "Minimum"
    private var currentMediumSuccess: String = "Medium"
    private var currentHighSuccess: String = "High"
    private var currentIsActive: Boolean = true

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogEditPieceBinding.inflate(layoutInflater)

        // Get arguments
        arguments?.let {
            pieceId = it.getLong(ARG_PIECE_ID, -1)
            currentName = it.getString(ARG_PIECE_NAME, "")
            currentColor = it.getString(ARG_PIECE_COLOR, "#66B2FF")
            currentPriority = TaskPriority.valueOf(it.getString(ARG_PIECE_PRIORITY, TaskPriority.LOW.name))
            currentTaskKind = TaskKind.valueOf(it.getString(ARG_TASK_KIND, TaskKind.STANDARD.name))
            currentMinimumSuccess = it.getString(ARG_MINIMUM_SUCCESS, "Minimum")
            currentMediumSuccess = it.getString(ARG_MEDIUM_SUCCESS, "Medium")
            currentHighSuccess = it.getString(ARG_HIGH_SUCCESS, "High")
            currentIsActive = it.getBoolean(ARG_IS_ACTIVE, true)
        }

        // Pre-populate fields
        binding.pieceNameEditText.setText(currentName)
        checkCurrentColor()
        binding.radioHighPriority.isChecked = currentPriority == TaskPriority.HIGH
        binding.radioLowPriority.isChecked = currentPriority == TaskPriority.LOW
        binding.minimumSuccessEditText.setText(currentMinimumSuccess)
        binding.mediumSuccessEditText.setText(currentMediumSuccess)
        binding.highSuccessEditText.setText(currentHighSuccess)
        binding.activeSwitch.isChecked = currentIsActive
        if (currentTaskKind == TaskKind.ROUTINE) {
            binding.titleTextView.text = "Edit Routine"
            binding.pieceNameInputLayout.hint = "Routine Name"
            binding.priorityLabel.visibility = android.view.View.GONE
            binding.priorityRadioGroup.visibility = android.view.View.GONE
            binding.minimumSuccessInputLayout.visibility = android.view.View.GONE
            binding.mediumSuccessInputLayout.visibility = android.view.View.GONE
            binding.highSuccessInputLayout.visibility = android.view.View.GONE
        }

        // Set up button listeners
        binding.saveButton.setOnClickListener {
            saveChanges()
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    private fun saveChanges() {
        val newName = binding.pieceNameEditText.text.toString().trim()

        if (newName.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a task name", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.updateTask(
            pieceId = pieceId,
            newName = newName,
            color = selectedTaskColor(),
            priority = if (currentTaskKind == TaskKind.ROUTINE) TaskPriority.LOW else if (binding.radioHighPriority.isChecked) TaskPriority.HIGH else TaskPriority.LOW,
            taskKind = currentTaskKind,
            minimumSuccess = thresholdText(binding.minimumSuccessEditText.text?.toString(), "Minimum"),
            mediumSuccess = thresholdText(binding.mediumSuccessEditText.text?.toString(), "Medium"),
            highSuccess = thresholdText(binding.highSuccessEditText.text?.toString(), "High"),
            isActive = binding.activeSwitch.isChecked
        )
        Toast.makeText(requireContext(), "Task updated", Toast.LENGTH_SHORT).show()

        dismiss()
    }

    private fun thresholdText(value: String?, fallback: String): String {
        return value?.trim().takeUnless { it.isNullOrBlank() } ?: fallback
    }

    private fun checkCurrentColor() {
        val checkedId = when (currentColor) {
            "#46C07A" -> binding.radioColorGreen.id
            "#F4C542" -> binding.radioColorYellow.id
            "#E86B6B" -> binding.radioColorRed.id
            "#9B7EDE" -> binding.radioColorPurple.id
            "#3FB8B8" -> binding.radioColorTeal.id
            "#F4884A" -> binding.radioColorOrange.id
            "#E05CB0" -> binding.radioColorPink.id
            "#8BC34A" -> binding.radioColorLime.id
            "#5C6BC0" -> binding.radioColorIndigo.id
            "#FFA726" -> binding.radioColorAmber.id
            "#26C6DA" -> binding.radioColorCyan.id
            else -> binding.radioColorBlue.id
        }
        binding.colorRadioGroup.check(checkedId)
    }

    private fun selectedTaskColor(): String {
        return when (binding.colorRadioGroup.checkedRadioButtonId) {
            binding.radioColorGreen.id -> "#46C07A"
            binding.radioColorYellow.id -> "#F4C542"
            binding.radioColorRed.id -> "#E86B6B"
            binding.radioColorPurple.id -> "#9B7EDE"
            binding.radioColorTeal.id -> "#3FB8B8"
            binding.radioColorOrange.id -> "#F4884A"
            binding.radioColorPink.id -> "#E05CB0"
            binding.radioColorLime.id -> "#8BC34A"
            binding.radioColorIndigo.id -> "#5C6BC0"
            binding.radioColorAmber.id -> "#FFA726"
            binding.radioColorCyan.id -> "#26C6DA"
            else -> "#66B2FF"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PIECE_ID = "piece_id"
        private const val ARG_PIECE_NAME = "piece_name"
        private const val ARG_PIECE_TYPE = "piece_type"
        private const val ARG_PIECE_COLOR = "piece_color"
        private const val ARG_PIECE_PRIORITY = "piece_priority"
        private const val ARG_TASK_KIND = "task_kind"
        private const val ARG_MINIMUM_SUCCESS = "minimum_success"
        private const val ARG_MEDIUM_SUCCESS = "medium_success"
        private const val ARG_HIGH_SUCCESS = "high_success"
        private const val ARG_IS_ACTIVE = "is_active"

        fun newInstance(
            pieceId: Long,
            pieceName: String,
            pieceType: ItemType,
            color: String,
            priority: TaskPriority,
            minimumSuccess: String,
            mediumSuccess: String,
            highSuccess: String,
            isActive: Boolean,
            taskKind: TaskKind = TaskKind.STANDARD
        ): EditPieceDialogFragment {
            return EditPieceDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_PIECE_ID, pieceId)
                    putString(ARG_PIECE_NAME, pieceName)
                    putString(ARG_PIECE_TYPE, pieceType.name)
                    putString(ARG_PIECE_COLOR, color)
                    putString(ARG_PIECE_PRIORITY, priority.name)
                    putString(ARG_TASK_KIND, taskKind.name)
                    putString(ARG_MINIMUM_SUCCESS, minimumSuccess)
                    putString(ARG_MEDIUM_SUCCESS, mediumSuccess)
                    putString(ARG_HIGH_SUCCESS, highSuccess)
                    putBoolean(ARG_IS_ACTIVE, isActive)
                }
            }
        }
    }
}
