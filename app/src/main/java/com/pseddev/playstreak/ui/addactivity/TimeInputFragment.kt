package com.pseddev.mystreak.ui.addactivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.pseddev.mystreak.MyStreakApplication
import com.pseddev.mystreak.databinding.FragmentTimeInputBinding

class TimeInputFragment : Fragment() {

    private var _binding: FragmentTimeInputBinding? = null
    private val binding get() = _binding!!

    private val args: TimeInputFragmentArgs by navArgs()

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
        _binding = FragmentTimeInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Handle back navigation in edit mode
        val editActivity = viewModel.editActivity.value
        if (editActivity != null) {
            val callback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack(com.pseddev.mystreak.R.id.addActivityFragment, true)
                }
            }
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        }

        // Pre-populate time input in edit mode
        viewModel.editActivity.observe(viewLifecycleOwner) { editActivity ->
            if (editActivity != null && editActivity.minutes > 0) {
                binding.editTextMinutes.setText(editActivity.minutes.toString())
            }
        }

        binding.buttonContinue.setOnClickListener {
            val minutesText = binding.editTextMinutes.text.toString()
            val minutes = if (minutesText.isNotEmpty()) {
                try {
                    minutesText.toInt()
                } catch (e: NumberFormatException) {
                    Toast.makeText(requireContext(), "Please enter a valid number", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            } else {
                5 // Default to 5 minutes if no input provided
            }

            navigateToSummary(minutes)
        }

        binding.buttonSkip.setOnClickListener {
            navigateToSummary(-1)
        }
    }

    private fun navigateToSummary(minutes: Int) {
        val action = TimeInputFragmentDirections
            .actionTimeInputFragmentToSummaryFragment(
                activityType = args.activityType,
                pieceId = args.pieceId,
                pieceName = args.pieceName,
                level = args.level,
                performanceType = args.performanceType,
                minutes = minutes
            )
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}