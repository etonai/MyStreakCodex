package com.pseddev.mystreak.ui.addactivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.pseddev.mystreak.MyStreakApplication
import com.pseddev.mystreak.databinding.FragmentSelectLevelBinding

class SelectLevelFragment : Fragment() {

    private var _binding: FragmentSelectLevelBinding? = null
    private val binding get() = _binding!!

    private val args: SelectLevelFragmentArgs by navArgs()

    private val viewModel: AddActivityViewModel by activityViewModels {
        AddActivityViewModelFactory(
            (requireActivity().application as MyStreakApplication).repository,
            requireContext()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectLevelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textPieceName.text = args.pieceName

        setupLevelOptions()
        setupTaskDescriptions()

        // Check if we're in edit mode and set up the ViewModel
        val editActivity = com.pseddev.mystreak.ui.progress.EditActivityStorage.getEditActivity()
        if (editActivity != null) {
            // Set edit mode in ViewModel
            viewModel.setEditMode(editActivity)

            val callback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    viewModel.abandonEditMode()
                    findNavController().popBackStack(com.pseddev.mystreak.R.id.progressFragment, false)
                }
            }
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        }

        // Pre-populate fields in edit mode
        viewModel.editActivity.observe(viewLifecycleOwner) { editActivity ->
            if (editActivity != null) {
                // Pre-select the level
                when (editActivity.level) {
                    1 -> binding.radioLevel1.isChecked = true
                    2 -> binding.radioLevel2.isChecked = true
                    3 -> binding.radioLevel3.isChecked = true
                    4 -> binding.radioLevel4.isChecked = true
                }

            }
        }

        binding.buttonContinue.setOnClickListener {
            val selectedLevel = when {
                binding.radioLevel1.isChecked -> 1
                binding.radioLevel2.isChecked -> 2
                binding.radioLevel3.isChecked -> 3
                binding.radioLevel4.isChecked -> 4
                else -> return@setOnClickListener
            }

            val action = SelectLevelFragmentDirections
                .actionSelectLevelFragmentToNotesInputFragment(
                    activityType = args.activityType,
                    pieceId = args.pieceId,
                    pieceName = args.pieceName,
                    level = selectedLevel,
                    performanceType = "activity"
                )
            findNavController().navigate(action)
        }
    }

    private fun setupLevelOptions() {
        binding.textLevelLabel.text = "Success Level:"
        binding.radioLevel1.text = "Minimum"
        binding.radioLevel2.text = "Medium"
        binding.radioLevel3.text = "High"
        binding.radioLevel4.visibility = View.GONE
        binding.performanceTypeGroup.visibility = View.GONE
    }

    private fun setupTaskDescriptions() {
        viewModel.getTask(args.pieceId).observe(viewLifecycleOwner) { task ->
            if (task != null) {
                binding.radioLevel1.text = "Minimum - ${task.minimumSuccess}"
                binding.radioLevel2.text = "Medium - ${task.mediumSuccess}"
                binding.radioLevel3.text = "High - ${task.highSuccess}"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
