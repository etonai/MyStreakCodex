package com.pseddev.mystreak.ui.achievements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.pseddev.mystreak.MyStreakApplication
import com.pseddev.mystreak.databinding.FragmentAchievementsBinding

class AchievementsFragment : Fragment() {

    private var _binding: FragmentAchievementsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AchievementsViewModel by viewModels {
        AchievementsViewModelFactory(
            (requireActivity().application as MyStreakApplication).repository,
            requireContext()
        )
    }

    private lateinit var adapter: AchievementsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAchievementsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = AchievementsAdapter()
        binding.achievementsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AchievementsFragment.adapter
        }
    }

    private fun observeViewModel() {
        viewModel.categorizedAchievements.observe(viewLifecycleOwner) { categories ->
            adapter.submitCategories(categories)
        }

        viewModel.achievementCounts.observe(viewLifecycleOwner) { (unlocked, total) ->
            binding.achievementCountText.text = "$unlocked/$total achievements"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}