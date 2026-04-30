package com.pseddev.playstreak.ui.addactivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.pseddev.playstreak.PlayStreakApplication
import com.pseddev.playstreak.data.entities.ItemType
import com.pseddev.playstreak.data.entities.PieceOrTechnique
import com.pseddev.playstreak.databinding.FragmentSelectPieceBinding

class SelectPieceFragment : Fragment() {
    
    private var _binding: FragmentSelectPieceBinding? = null
    private val binding get() = _binding!!
    
    private val args: SelectPieceFragmentArgs by navArgs()
    
    private val viewModel: AddActivityViewModel by activityViewModels {
        AddActivityViewModelFactory(
            (requireActivity().application as PlayStreakApplication).repository,
            requireContext()
        )
    }
    
    private lateinit var adapter: PieceAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectPieceBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupObservers()
        
        binding.buttonAddNew.setOnClickListener {
            val action = SelectPieceFragmentDirections
                .actionSelectPieceFragmentToAddNewPieceFragment(args.activityType)
            findNavController().navigate(action)
        }
    }
    
    private fun navigateToSelectLevel(pieceId: Long, pieceName: String, itemType: ItemType) {
        val action = SelectPieceFragmentDirections
            .actionSelectPieceFragmentToSelectLevelFragment(
                activityType = args.activityType,
                pieceId = pieceId,
                pieceName = pieceName,
                itemType = itemType
            )
        findNavController().navigate(action)
    }
    
    private fun setupRecyclerView() {
        adapter = PieceAdapter { piece ->
            navigateToSelectLevel(piece.id, piece.name, piece.type)
        }
        
        binding.recyclerViewPieces.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SelectPieceFragment.adapter
        }
    }
    
    private fun setupObservers() {
        viewModel.getFavorites().observe(viewLifecycleOwner) { favorites ->
            viewModel.getPiecesAndTechniques(args.activityType).observe(viewLifecycleOwner) { all ->
                val groupedItems = mutableListOf<PieceAdapterItem>()
                
                if (favorites.isNotEmpty()) {
                    groupedItems.add(PieceAdapterItem.Header("High Priority:"))
                    groupedItems.addAll(favorites.map { PieceAdapterItem.Item(it) })
                }
                
                if (all.isNotEmpty()) {
                    groupedItems.add(PieceAdapterItem.Header("All Tasks:"))
                    groupedItems.addAll(all.map { PieceAdapterItem.Item(it) })
                }
                
                adapter.submitList(groupedItems)
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
