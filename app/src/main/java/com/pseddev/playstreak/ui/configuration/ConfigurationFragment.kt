package com.pseddev.mystreak.ui.configuration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.pseddev.mystreak.databinding.FragmentConfigurationBinding
import com.pseddev.mystreak.utils.PreferencesManager
import com.pseddev.mystreak.utils.ConfigurationManager

class ConfigurationFragment : Fragment() {

    private var _binding: FragmentConfigurationBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ConfigurationViewModel
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var configurationManager: ConfigurationManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfigurationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferencesManager = PreferencesManager.getInstance(requireContext())
        configurationManager = ConfigurationManager.getInstance(requireContext())

        // Create ViewModel with custom factory
        viewModel = ViewModelProvider(this, ConfigurationViewModelFactory(preferencesManager))
            .get(ConfigurationViewModel::class.java)

        setupObservers()
        setupClickListeners()
        setupDarkModeToggle()
        setupPruningToggle()
        setupAchievementCelebrationsToggle()
    }

    private fun setupObservers() {
        viewModel.calendarDetailModeEnabled.observe(viewLifecycleOwner) { enabled ->
            // Update switch state without triggering listener
            binding.switchCalendarDetailMode.setOnCheckedChangeListener(null)
            binding.switchCalendarDetailMode.isChecked = enabled
            binding.switchCalendarDetailMode.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setCalendarDetailMode(isChecked)
            }
        }
    }

    private fun setupClickListeners() {
        binding.switchCalendarDetailMode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setCalendarDetailMode(isChecked)
        }
    }

    private fun setupDarkModeToggle() {
        binding.switchDarkMode.isChecked = configurationManager.isDarkModeEnabled()
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            configurationManager.setDarkModeEnabled(isChecked)
            val nightMode = if (isChecked) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
            AppCompatDelegate.setDefaultNightMode(nightMode)
        }
    }

    private fun setupPruningToggle() {
        // Set initial state from ConfigurationManager
        binding.switchAllowPruning.isChecked = configurationManager.isPruningEnabled()

        // Set up toggle listener
        binding.switchAllowPruning.setOnCheckedChangeListener { _, isChecked ->
            configurationManager.setPruningEnabled(isChecked)
        }
    }

    private fun setupAchievementCelebrationsToggle() {
        // Set initial state from ConfigurationManager
        binding.switchAchievementCelebrations.isChecked = configurationManager.isAchievementCelebrationEnabled()

        // Set up toggle listener
        binding.switchAchievementCelebrations.setOnCheckedChangeListener { _, isChecked ->
            configurationManager.setAchievementCelebrationEnabled(isChecked)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class ConfigurationViewModelFactory(
    private val preferencesManager: PreferencesManager
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConfigurationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ConfigurationViewModel(preferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
