package com.normathi.soniclean.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.normathi.soniclean.data.repository.DefaultSettingsRepository
import com.normathi.soniclean.data.repository.SettingsRepository
import com.normathi.soniclean.data.repository.UserSettings
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel @JvmOverloads constructor(
    application: Application,
    private val settingsRepository: SettingsRepository = DefaultSettingsRepository.getInstance(application)
) : AndroidViewModel(application) {

    val settings: StateFlow<UserSettings> = settingsRepository.settings

    fun setTheme(theme: String) {
        settingsRepository.setTheme(theme)
    }

    fun setHapticsEnabled(enabled: Boolean) {
        settingsRepository.setHapticsEnabled(enabled)
    }

    fun setSafetyReminders(enabled: Boolean) {
        settingsRepository.setSafetyReminders(enabled)
    }

    fun setDefaultOutput(output: String) {
        settingsRepository.setDefaultOutput(output)
    }

    fun setQuickCleanDurationSec(seconds: Int) {
        settingsRepository.setQuickCleanDurationSec(seconds)
    }
}
