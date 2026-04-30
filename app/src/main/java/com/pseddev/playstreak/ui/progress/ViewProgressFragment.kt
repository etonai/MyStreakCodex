package com.pseddev.playstreak.ui.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.pseddev.playstreak.databinding.FragmentViewProgressBinding
import com.pseddev.playstreak.R

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
        
        setupBottomNavigation()
    }
    
    private fun setupBottomNavigation() {
        val pagerAdapter = ViewProgressPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter
        binding.viewPager.isUserInputEnabled = true

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val page = when (item.itemId) {
                R.id.navigation_dashboard -> 0
                R.id.navigation_calendar -> 1
                R.id.navigation_tasks -> 2
                else -> return@setOnItemSelectedListener false
            }
            binding.viewPager.currentItem = page
            true
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val itemId = when (position) {
                    0 -> R.id.navigation_dashboard
                    1 -> R.id.navigation_calendar
                    2 -> R.id.navigation_tasks
                    else -> return
                }
                if (binding.bottomNavigation.selectedItemId != itemId) {
                    binding.bottomNavigation.selectedItemId = itemId
                }
            }
        })
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
