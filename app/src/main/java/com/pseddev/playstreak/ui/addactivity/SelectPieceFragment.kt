package com.pseddev.mystreak.ui.addactivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.pseddev.mystreak.MyStreakApplication
import com.pseddev.mystreak.data.entities.ItemType
import com.pseddev.mystreak.data.entities.PieceOrTechnique
import com.pseddev.mystreak.data.entities.TaskKind
import com.pseddev.mystreak.databinding.FragmentSelectPieceBinding

class SelectPieceFragment : Fragment() {

    private var _binding: FragmentSelectPieceBinding? = null
    private val binding get() = _binding!!

    private val args: SelectPieceFragmentArgs by navArgs()

    private val viewModel: AddActivityViewModel by activityViewModels {
        AddActivityViewModelFactory(
            (requireActivity().application as MyStreakApplication).repository,
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

    private fun navigateToRoutineNotes(pieceId: Long, pieceName: String) {
        val action = SelectPieceFragmentDirections
            .actionSelectPieceFragmentToNotesInputFragment(
                activityType = args.activityType,
                pieceId = pieceId,
                pieceName = pieceName,
                level = 3,
                performanceType = "routine"
            )
        findNavController().navigate(action)
    }

    private fun setupRecyclerView() {
        adapter = PieceAdapter { piece ->
            if (piece.taskKind == TaskKind.ROUTINE) {
                navigateToRoutineNotes(piece.id, piece.name)
            } else {
                navigateToSelectLevel(piece.id, piece.name, piece.type)
            }
        }

        binding.recyclerViewPieces.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SelectPieceFragment.adapter
        }
    }

    private fun setupObservers() {
        viewModel.getFavorites().observe(viewLifecycleOwner) { favorites ->
            viewModel.getPiecesAndTechniques(args.activityType).observe(viewLifecycleOwner) { all ->
                viewModel.getRoutineTasks().observe(viewLifecycleOwner) { routines ->
                    val groupedItems = mutableListOf<PieceAdapterItem>()
                    val highPriorityIds = favorites.map { it.id }.toSet()
                    val remainingTasks = all.filter { it.id !in highPriorityIds }

                    if (favorites.isNotEmpty()) {
                        groupedItems.add(PieceAdapterItem.Header("High Priority:"))
                        groupedItems.addAll(favorites.map { PieceAdapterItem.Item(it) })
                    }

                    if (remainingTasks.isNotEmpty()) {
                        groupedItems.add(PieceAdapterItem.Header("Low Priority:"))
                        groupedItems.addAll(remainingTasks.map { PieceAdapterItem.Item(it) })
                    }

                    if (routines.isNotEmpty()) {
                        groupedItems.add(PieceAdapterItem.Header("Routine:"))
                        groupedItems.addAll(routines.map { PieceAdapterItem.Item(it) })
                    }

                    adapter.submitList(groupedItems)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
