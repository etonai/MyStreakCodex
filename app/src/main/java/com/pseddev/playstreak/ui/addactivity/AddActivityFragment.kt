package com.pseddev.playstreak.ui.addactivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.pseddev.playstreak.PlayStreakApplication
import com.pseddev.playstreak.data.entities.ActivityType
import com.pseddev.playstreak.ui.progress.EditActivityStorage
import com.pseddev.playstreak.databinding.FragmentAddActivityBinding

class AddActivityFragment : Fragment() {
    
    private var _binding: FragmentAddActivityBinding? = null
    private val binding get() = _binding!!
    
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
        _binding = FragmentAddActivityBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Setup button click listeners
        binding.buttonPractice.setOnClickListener {
            viewModel.clearEditMode()
            EditActivityStorage.clearEditActivity()
            val action = AddActivityFragmentDirections
                .actionAddActivityFragmentToSelectPieceFragment(ActivityType.PRACTICE)
            findNavController().navigate(action)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
