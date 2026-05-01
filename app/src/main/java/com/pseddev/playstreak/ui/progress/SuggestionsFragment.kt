package com.pseddev.mystreak.ui.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.pseddev.mystreak.BuildConfig
import com.pseddev.mystreak.MyStreakApplication
import com.pseddev.mystreak.R
import com.pseddev.mystreak.databinding.FragmentSuggestionsBinding
import com.pseddev.mystreak.utils.ProUserManager

class SuggestionsFragment : Fragment() {

    private var _binding: FragmentSuggestionsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SuggestionsViewModel by viewModels {
        SuggestionsViewModelFactory(
            (requireActivity().application as MyStreakApplication).repository,
            requireContext()
        )
    }

    private lateinit var practiceAdapter: SuggestionsAdapter
    private lateinit var performanceAdapter: SuggestionsAdapter
    private lateinit var proUserManager: ProUserManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSuggestionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ProUserManager
        proUserManager = ProUserManager.getInstance(requireContext())

        val onAddActivityClick = { suggestionItem: SuggestionItem ->
            // Show quick add activity dialog with piece pre-filled
            val dialog = QuickAddActivityDialogFragment.newInstance(
                suggestionItem.piece.id,
                suggestionItem.piece.name,
                "suggestion"
            )
            dialog.show(parentFragmentManager, "QuickAddActivityDialog")
        }

        practiceAdapter = SuggestionsAdapter(onAddActivityClick)
        performanceAdapter = SuggestionsAdapter(onAddActivityClick)

        binding.practiceRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.practiceRecyclerView.adapter = practiceAdapter
        binding.practiceRecyclerView.setHasFixedSize(false)
        binding.practiceRecyclerView.isNestedScrollingEnabled = false

        // Set up performance RecyclerView
        binding.performanceRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.performanceRecyclerView.adapter = performanceAdapter
        binding.performanceRecyclerView.setHasFixedSize(false)
        binding.performanceRecyclerView.isNestedScrollingEnabled = false


        // Observe practice suggestions separately
        viewModel.practiceSuggestions.observe(viewLifecycleOwner) { allPracticeSuggestions ->
            if (BuildConfig.DEBUG) {
                android.util.Log.d("Phase4Debug05", "SuggestionsTab received ${allPracticeSuggestions.size} practice suggestions")
            }

            // Apply Free/Pro user limits to practice suggestions
            val favoriteLimit = if (proUserManager.isProUser())
                ProUserManager.PRO_USER_PRACTICE_FAVORITE_SUGGESTIONS
                else ProUserManager.FREE_USER_PRACTICE_FAVORITE_SUGGESTIONS
            val nonFavoriteLimit = if (proUserManager.isProUser())
                ProUserManager.PRO_USER_PRACTICE_NON_FAVORITE_SUGGESTIONS
                else ProUserManager.FREE_USER_PRACTICE_NON_FAVORITE_SUGGESTIONS

            val favoriteSuggestions = allPracticeSuggestions.filter { it.piece.isFavorite }.take(favoriteLimit)
            val nonFavoriteSuggestions = allPracticeSuggestions.filter { !it.piece.isFavorite }.take(nonFavoriteLimit)
            val practiceSuggestions = favoriteSuggestions + nonFavoriteSuggestions

            if (BuildConfig.DEBUG) {
                android.util.Log.d("Phase4Debug06", "SuggestionsTab limits: favorite=$favoriteLimit, nonFavorite=$nonFavoriteLimit")
                android.util.Log.d("Phase4Debug07", "SuggestionsTab after limiting: ${practiceSuggestions.size} practice total")
                android.util.Log.d("Phase4Debug08", "SuggestionsTab practice: ${favoriteSuggestions.size} favorite, ${nonFavoriteSuggestions.size} non-favorite")

                // Log first few suggestions for comparison
                practiceSuggestions.take(5).forEachIndexed { index, suggestion ->
                    android.util.Log.d("Phase4Debug09", "SuggestionsTab[$index]: ${suggestion.piece.name} (favorite=${suggestion.piece.isFavorite})")
                }
            }

            // Update practice suggestions
            practiceAdapter.submitList(practiceSuggestions)
            if (practiceSuggestions.isEmpty()) {
                binding.practiceRecyclerView.visibility = View.GONE
                binding.noPracticeSuggestionsText.visibility = View.VISIBLE
            } else {
                binding.practiceRecyclerView.visibility = View.VISIBLE
                binding.noPracticeSuggestionsText.visibility = View.GONE
            }

            updateEmptyState()
        }

        // Observe performance suggestions separately
        viewModel.performanceSuggestions.observe(viewLifecycleOwner) { performanceSuggestions ->
            if (BuildConfig.DEBUG) {
                android.util.Log.d("Phase4Debug82", "SuggestionsTab received ${performanceSuggestions.size} performance suggestions")
            }

            // Show performance suggestions for Pro users
            if (proUserManager.isProUser()) {
                if (BuildConfig.DEBUG) {
                    android.util.Log.d("Phase4Debug81", "Pro user detected, showing performance suggestions")
                    performanceSuggestions.forEachIndexed { index, suggestion ->
                        android.util.Log.d("Phase4Debug83", "PerfSugg[$index]: ${suggestion.piece.name} | ${suggestion.suggestionReason}")
                    }
                }

                binding.performanceSuggestionsHeader.visibility = View.VISIBLE
                performanceAdapter.submitList(performanceSuggestions)

                if (performanceSuggestions.isEmpty()) {
                    if (BuildConfig.DEBUG) {
                        android.util.Log.d("Phase4Debug84", "No performance suggestions, showing 'no suggestions' text")
                    }
                    binding.performanceRecyclerView.visibility = View.GONE
                    binding.noPerformanceSuggestionsText.visibility = View.VISIBLE
                } else {
                    if (BuildConfig.DEBUG) {
                        android.util.Log.d("Phase4Debug85", "Showing ${performanceSuggestions.size} performance suggestions")
                    }
                    binding.performanceRecyclerView.visibility = View.VISIBLE
                    binding.noPerformanceSuggestionsText.visibility = View.GONE
                }
            } else {
                // Hide performance suggestions for non-Pro users
                binding.performanceSuggestionsHeader.visibility = View.GONE
                binding.performanceRecyclerView.visibility = View.GONE
                binding.noPerformanceSuggestionsText.visibility = View.GONE
            }

            updateEmptyState()
        }
    }

    private fun updateEmptyState() {
        // This will be called from both observers, but we need to check both states
        val practiceCount = practiceAdapter.itemCount
        val performanceCount = if (proUserManager.isProUser()) performanceAdapter.itemCount else 0

        val hasAnySuggestions = practiceCount > 0 || performanceCount > 0
        if (hasAnySuggestions) {
            binding.emptyView.visibility = View.GONE
        } else {
            binding.emptyView.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}