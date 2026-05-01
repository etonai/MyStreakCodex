package com.pseddev.mystreak.ui.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.pseddev.mystreak.MyStreakApplication
import com.pseddev.mystreak.databinding.FragmentAbandonedBinding
import com.pseddev.mystreak.utils.ProUserManager

class AbandonedFragment : Fragment() {

    private var _binding: FragmentAbandonedBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AbandonedViewModel by viewModels {
        AbandonedViewModelFactory(
            (requireActivity().application as MyStreakApplication).repository
        )
    }

    private lateinit var adapter: AbandonedAdapter
    private var shouldScrollToTop = false
    private lateinit var proUserManager: ProUserManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAbandonedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        proUserManager = ProUserManager.getInstance(requireContext())
        setupSortControls()

        adapter = AbandonedAdapter(
            onAddActivityClick = { abandonedItem ->
                showQuickAddActivityDialog(abandonedItem)
            },
            proUserManager = proUserManager
        )
        binding.abandonedRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.abandonedRecyclerView.adapter = adapter

        // Enable nested scrolling for proper interaction with ViewPager2
        binding.abandonedRecyclerView.isNestedScrollingEnabled = true

        viewModel.abandonedPieces.observe(viewLifecycleOwner) { pieces ->
            if (pieces.isEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
                binding.abandonedRecyclerView.visibility = View.GONE
            } else {
                binding.emptyView.visibility = View.GONE
                binding.abandonedRecyclerView.visibility = View.VISIBLE
                adapter.submitList(pieces) {
                    // Scroll to top after the new data is submitted if a sort change happened
                    if (shouldScrollToTop) {
                        binding.abandonedRecyclerView.scrollToPosition(0)
                        shouldScrollToTop = false
                    }
                }
            }
        }
    }

    private fun setupSortControls() {
        // Set up sort direction button
        binding.buttonSortDirection.setOnClickListener {
            viewModel.toggleSortDirection()
            updateSortDirectionButton()
            shouldScrollToTop = true
        }

        // Initialize sort direction button
        updateSortDirectionButton()
    }

    private fun updateSortDirectionButton() {
        val direction = viewModel.getCurrentSortDirection()
        binding.buttonSortDirection.text = if (direction == AbandonedSortDirection.ASCENDING) "↑" else "↓"
    }

    private fun showQuickAddActivityDialog(abandonedItem: AbandonedItem) {
        val dialog = QuickAddActivityDialogFragment.newInstance(
            abandonedItem.piece.id,
            abandonedItem.piece.name,
            "dashboard_quick"
        )
        dialog.show(parentFragmentManager, "QuickAddActivityDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}