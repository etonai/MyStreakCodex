package com.pseddev.mystreak.ui.configuration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pseddev.mystreak.utils.PreferencesManager

class ConfigurationViewModel(private val preferencesManager: PreferencesManager) : ViewModel() {

    private val _calendarDetailModeEnabled = MutableLiveData<Boolean>()
    val calendarDetailModeEnabled: LiveData<Boolean> = _calendarDetailModeEnabled

    init {
        // Load current preference value
        _calendarDetailModeEnabled.value = preferencesManager.isCalendarDetailModeEnabled()
    }

    /**
     * Toggle calendar activities detail mode and update preference
     */
    fun toggleCalendarDetailMode() {
        val newValue = preferencesManager.toggleCalendarDetailMode()
        _calendarDetailModeEnabled.value = newValue
    }

    /**
     * Set calendar activities detail mode to specific value
     */
    fun setCalendarDetailMode(enabled: Boolean) {
        preferencesManager.setCalendarDetailModeEnabled(enabled)
        _calendarDetailModeEnabled.value = enabled
    }
}