/*
 * Timeline Tab Temporarily Disabled for Evaluation
 * 
 * DevCycle 2025-0005 Phase 4: Timeline tab has been temporarily removed to evaluate
 * user preference for Calendar Activities Detail Mode vs. separate Timeline functionality.
 * 
 * RESTORATION PROCESS:
 * 1. In setupTabs() TabLayoutMediator: Uncomment line 65 and move line 66 to position 5
 * 2. In ViewProgressPagerAdapter: Change getItemCount() back to (6/5) and uncomment line 106
 * 3. Restore data loading in TimelineViewModel.kt and TimelineFragment.kt
 * 
 * All Timeline code is preserved via comments for quick restoration.
 */
package com.pseddev.playstreak.ui.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pseddev.playstreak.databinding.FragmentViewProgressBinding
import com.google.android.material.tabs.TabLayoutMediator

class ViewProgressFragment : Fragment() {
    
    private var _binding: FragmentViewProgressBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewProgressBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupTabs()
    }
    
    private fun setupTabs() {
        val pagerAdapter = ViewProgressPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter
        
        // Improve nested scrolling for RecyclerViews inside ViewPager2
        binding.viewPager.isUserInputEnabled = true
        
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Dashboard"
                1 -> "Calendar"
                2 -> "Tasks"
                else -> ""
            }
        }.attach()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    private inner class ViewProgressPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 3
        
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> DashboardFragment()
                1 -> CalendarFragment()
                2 -> PiecesFragment()
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }
    }
}
