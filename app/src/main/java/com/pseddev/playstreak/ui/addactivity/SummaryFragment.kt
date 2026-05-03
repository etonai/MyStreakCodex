package com.pseddev.mystreak.ui.addactivity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.pseddev.mystreak.MyStreakApplication
import com.pseddev.mystreak.R
import com.pseddev.mystreak.databinding.FragmentSummaryBinding
import com.pseddev.mystreak.ui.progress.successLevelDescription
import com.pseddev.mystreak.ui.progress.successLevelFromActivityLevel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SummaryFragment : Fragment() {

    private var _binding: FragmentSummaryBinding? = null
    private val binding get() = _binding!!

    private val args: SummaryFragmentArgs by navArgs()

    private val viewModel: AddActivityViewModel by activityViewModels {
        AddActivityViewModelFactory(
            (requireActivity().application as MyStreakApplication).repository,
            requireContext()
        )
    }

    private var currentTimestamp: Long = System.currentTimeMillis()
    private var saveInProgress = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize timestamp - use edit activity timestamp if in edit mode, otherwise current time
        val editActivity = viewModel.editActivity.value
        val isEditMode = com.pseddev.mystreak.ui.progress.EditActivityStorage.isEditMode()
        currentTimestamp = if (editActivity != null && isEditMode) {
            editActivity.timestamp
        } else {
            System.currentTimeMillis()
        }

        // Handle back navigation in edit mode
        if (editActivity != null) {
            val callback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // In edit mode, go back to Timeline (progressFragment)
                    finishActivityFlow()
                }
            }
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        }

        setupSummary()
        setupDateTimeEditing()

        binding.buttonSave.setOnClickListener {
            if (saveInProgress) {
                return@setOnClickListener
            }
            if (currentTimestamp > System.currentTimeMillis()) {
                Toast.makeText(requireContext(), "Activities cannot be dated in the future.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            saveInProgress = true
            binding.buttonSave.isEnabled = false
            binding.buttonCancel.isEnabled = false

            val editActivity = viewModel.editActivity.value
            if (editActivity != null) {
                // Edit mode - update existing activity
                viewModel.updateActivity(
                    activityId = editActivity.id,
                    pieceId = args.pieceId,
                    activityType = args.activityType,
                    level = args.level,
                    performanceType = args.performanceType,
                    minutes = args.minutes,
                    notes = args.notes,
                    timestamp = currentTimestamp  // Use potentially modified timestamp
                )
            } else {
                // Add mode - save new activity
                viewModel.saveActivity(
                    pieceId = args.pieceId,
                    activityType = args.activityType,
                    level = args.level,
                    performanceType = args.performanceType,
                    minutes = args.minutes,
                    notes = args.notes,
                    timestamp = currentTimestamp
                )
            }
        }

        binding.buttonCancel.setOnClickListener {
            binding.buttonSave.isEnabled = false
            binding.buttonCancel.isEnabled = false
            finishActivityFlow()
        }

        viewModel.navigateToMain.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                viewModel.doneNavigating()
                finishActivityFlow()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                saveInProgress = false
                binding.buttonSave.isEnabled = true
                binding.buttonCancel.isEnabled = true
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearErrorMessage()
            }
        }

        // Observe celebration events
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

    private fun setupSummary() {
        binding.textPiece.text = "Task: ${args.pieceName}"
        binding.textType.visibility = View.GONE

        binding.textLevel.text = "Success: Level ${args.level}"
        viewModel.getTask(args.pieceId).observe(viewLifecycleOwner) { task ->
            val successLevel = successLevelFromActivityLevel(args.level)
            binding.textLevel.text = if (task != null && successLevel != null) {
                "Success: ${successLevelDescription(successLevel, task)}"
            } else {
                "Success: Level ${args.level}"
            }
        }

        binding.textTime.visibility = View.GONE

        val dateFormat = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.US)
        val editActivity = viewModel.editActivity.value
        if (editActivity != null) {
            binding.textTitle.text = "Edit Activity"
            // Edit mode - show edit buttons and current timestamp
            binding.textDate.text = "Date: ${dateFormat.format(Date(currentTimestamp))}"
            binding.buttonSave.text = "Update"
            binding.buttonEditDate.visibility = View.VISIBLE
            binding.buttonEditTime.visibility = View.VISIBLE
        } else {
            binding.textTitle.text = "Add Activity"
            // Add mode - show current date, no edit buttons
            binding.textDate.text = "Date: ${dateFormat.format(Date(currentTimestamp))}"
            binding.buttonSave.text = "Save"
            binding.buttonEditDate.visibility = View.GONE
            binding.buttonEditTime.visibility = View.GONE
        }

        binding.textNotes.visibility = View.GONE
    }

    private fun setupDateTimeEditing() {
        binding.buttonEditDate.setOnClickListener {
            showDatePicker()
        }

        binding.buttonEditTime.setOnClickListener {
            showTimePicker()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentTimestamp

        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                // Update the date part of currentTimestamp
                val newCalendar = Calendar.getInstance()
                newCalendar.timeInMillis = currentTimestamp
                newCalendar.set(Calendar.YEAR, year)
                newCalendar.set(Calendar.MONTH, month)
                newCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                currentTimestamp = newCalendar.timeInMillis
                updateDateDisplay()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.datePicker.maxDate = System.currentTimeMillis()

        datePicker.show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentTimestamp

        val timePicker = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                // Update the time part of currentTimestamp
                val newCalendar = Calendar.getInstance()
                newCalendar.timeInMillis = currentTimestamp
                newCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                newCalendar.set(Calendar.MINUTE, minute)
                newCalendar.set(Calendar.SECOND, 0)
                newCalendar.set(Calendar.MILLISECOND, 0)

                currentTimestamp = minOf(newCalendar.timeInMillis, System.currentTimeMillis())
                updateDateDisplay()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false // Use 12-hour format
        )

        timePicker.show()
    }

    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.US)
        binding.textDate.text = "Date: ${dateFormat.format(Date(currentTimestamp))}"
    }

    private fun finishActivityFlow() {
        val navController = findNavController()
        if (navController.popBackStack(R.id.progressFragment, false)) {
            return
        }

        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.nav_graph, true)
            .build()
        navController.navigate(R.id.progressFragment, null, navOptions)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
