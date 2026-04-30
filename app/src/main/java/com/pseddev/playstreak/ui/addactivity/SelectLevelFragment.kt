package com.pseddev.playstreak.ui.addactivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.pseddev.playstreak.PlayStreakApplication
import com.pseddev.playstreak.data.entities.ActivityType
import com.pseddev.playstreak.data.entities.ItemType
import com.pseddev.playstreak.databinding.FragmentSelectLevelBinding

class SelectLevelFragment : Fragment() {
    
    private var _binding: FragmentSelectLevelBinding? = null
    private val binding get() = _binding!!
    
    private val args: SelectLevelFragmentArgs by navArgs()
    
    private val viewModel: AddActivityViewModel by activityViewModels {
        AddActivityViewModelFactory(
            (requireActivity().application as PlayStreakApplication).repository,
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
        
        // Check if we're in edit mode and set up the ViewModel
        val editActivity = com.pseddev.playstreak.ui.progress.EditActivityStorage.getEditActivity()
        if (editActivity != null) {
            // Set edit mode in ViewModel
            viewModel.setEditMode(editActivity)
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
                
                // Pre-select performance type if applicable
                if (args.activityType == ActivityType.PERFORMANCE) {
                    when (editActivity.performanceType) {
                        "online" -> binding.radioPerformanceOnline.isChecked = true
                        "live" -> binding.radioPerformanceLive.isChecked = true
                    }
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
            
            val performanceType = if (args.activityType == ActivityType.PERFORMANCE) {
                when {
                    binding.radioPerformanceOnline.isChecked -> "online"
                    binding.radioPerformanceLive.isChecked -> "live"
                    else -> "practice"
                }
            } else {
                "practice"
            }
            
            // Determine next navigation
            when {
                // Notes input for legacy Performance records.
                args.activityType == ActivityType.PERFORMANCE -> {
                    val action = SelectLevelFragmentDirections
                        .actionSelectLevelFragmentToNotesInputFragment(
                            activityType = args.activityType,
                            pieceId = args.pieceId,
                            pieceName = args.pieceName,
                            level = selectedLevel,
                            performanceType = performanceType
                        )
                    findNavController().navigate(action)
                }
                // Direct to summary
                else -> {
                    val action = SelectLevelFragmentDirections
                        .actionSelectLevelFragmentToSummaryFragment(
                            activityType = args.activityType,
                            pieceId = args.pieceId,
                            pieceName = args.pieceName,
                            level = selectedLevel,
                            performanceType = performanceType
                        )
                    findNavController().navigate(action)
                }
            }
        }
    }
    
    private fun setupLevelOptions() {
        if (args.activityType == ActivityType.PRACTICE) {
            binding.textLevelLabel.text = "Success Level:"
            binding.radioLevel1.text = "Minimum"
            binding.radioLevel2.text = "Medium"
            binding.radioLevel3.text = "High"
            binding.radioLevel4.apply {
                text = "High"
                visibility = View.GONE
            }
            binding.performanceTypeGroup.visibility = View.GONE
        } else {
            binding.textLevelLabel.text = "Success Level:"
            binding.radioLevel1.text = "Minimum"
            binding.radioLevel2.text = "Medium"
            binding.radioLevel3.text = "High"
            binding.radioLevel4.visibility = View.GONE
            binding.performanceTypeGroup.visibility = View.GONE
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
