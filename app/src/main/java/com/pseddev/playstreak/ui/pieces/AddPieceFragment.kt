package com.pseddev.playstreak.ui.pieces

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.pseddev.playstreak.PlayStreakApplication
import com.pseddev.playstreak.data.entities.ItemType
import com.pseddev.playstreak.databinding.FragmentAddPieceBinding

class AddPieceFragment : Fragment() {
    
    private var _binding: FragmentAddPieceBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AddPieceViewModel by viewModels {
        AddPieceViewModelFactory(
            (requireActivity().application as PlayStreakApplication).repository,
            requireContext()
        )
    }
    
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
            
            val type = ItemType.PIECE
            val isFavorite = false
            
            viewModel.savePiece(name, type, isFavorite)
        }
        
        binding.cancelButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun observeViewModel() {
        viewModel.saveResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is AddPieceResult.Success -> {
                    Toast.makeText(requireContext(), "Task added successfully", Toast.LENGTH_SHORT).show()
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
                val achievement = com.pseddev.playstreak.utils.AchievementDefinitions.getAllAchievementDefinitions()
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
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
