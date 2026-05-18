package com.pseddev.mystreak.ui.pieces

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.pseddev.mystreak.MyStreakApplication
import com.pseddev.mystreak.data.entities.TaskKind
import com.pseddev.mystreak.data.entities.TaskPriority
import com.pseddev.mystreak.databinding.FragmentAddPieceBinding
import com.pseddev.mystreak.utils.TaskColors

class AddPieceFragment : Fragment() {

    private var _binding: FragmentAddPieceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddPieceViewModel by viewModels {
        AddPieceViewModelFactory(
            (requireActivity().application as MyStreakApplication).repository,
            requireContext()
        )
    }
    private val taskKind: TaskKind
        get() = TaskKind.valueOf(arguments?.getString("taskKind", TaskKind.STANDARD.name) ?: TaskKind.STANDARD.name)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPieceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupModeUi()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.saveButton.setOnClickListener {
            val name = binding.pieceNameEditText.text?.toString()?.trim()

            if (name.isNullOrBlank()) {
                binding.pieceNameInputLayout.error = "Please enter a task name"
                return@setOnClickListener
            }

            binding.pieceNameInputLayout.error = null

            viewModel.saveTask(
                name = name,
                color = TaskColors.storedColorFor(taskKind, selectedTaskColor()),
                priority = if (binding.radioHighPriority.isChecked) TaskPriority.HIGH else TaskPriority.LOW,
                taskKind = taskKind,
                minimumSuccess = thresholdText(binding.minimumSuccessEditText.text?.toString(), "Minimum"),
                mediumSuccess = thresholdText(binding.mediumSuccessEditText.text?.toString(), "Medium"),
                highSuccess = thresholdText(binding.highSuccessEditText.text?.toString(), "High"),
                isActive = binding.activeSwitch.isChecked
            )
        }

        binding.cancelButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeViewModel() {
        viewModel.saveResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is AddPieceResult.Success -> {
                    val label = if (taskKind == TaskKind.ROUTINE) "Routine" else "Task"
                    Toast.makeText(requireContext(), "$label added successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is AddPieceResult.Error -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                }
                is AddPieceResult.PieceLimitReached -> {
                    showPieceLimitDialog(result.currentCount, result.limit, result.isProUser)
                }
                is AddPieceResult.DuplicateName -> {
                    binding.pieceNameInputLayout.error = "This task already exists"
                }
            }
        }

        viewModel.canAddFavorites.observe(viewLifecycleOwner) {
            binding.favoriteSwitch.visibility = View.GONE
        }

        viewModel.showCelebration.observe(viewLifecycleOwner) { achievementType ->
            if (achievementType != null) {
                val achievement = com.pseddev.mystreak.utils.AchievementDefinitions.getAllAchievementDefinitions()
                    .find { it.type == achievementType }
                if (achievement != null) {
                    viewModel.getCelebrationManager().showCelebration(binding.root, achievement)
                }
                viewModel.onCelebrationHandled()
            }
        }
    }

    private fun showPieceLimitDialog(currentCount: Int, limit: Int, isProUser: Boolean) {
        val message = "Task Limit Reached\n\n" +
                "You currently have $currentCount tasks. " +
                "This app supports up to $limit tasks total to ensure good performance.\n\n" +
                "You can keep all your existing tasks, but cannot add new ones until you remove some tasks."

        AlertDialog.Builder(requireContext())
            .setTitle("Cannot Add Task")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun setupModeUi() {
        if (taskKind == TaskKind.ROUTINE) {
            binding.titleTextView.text = "Add New Routine"
            binding.pieceNameInputLayout.hint = "Routine Name"
            binding.priorityLabel.visibility = View.GONE
            binding.priorityRadioGroup.visibility = View.GONE
            binding.colorLabel.visibility = View.GONE
            binding.colorRadioGroup.visibility = View.GONE
            binding.minimumSuccessInputLayout.visibility = View.GONE
            binding.mediumSuccessInputLayout.visibility = View.GONE
            binding.highSuccessInputLayout.visibility = View.GONE
            binding.saveButton.text = "Save Routine"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun thresholdText(value: String?, fallback: String): String {
        return value?.trim().takeUnless { it.isNullOrBlank() } ?: fallback
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
}
