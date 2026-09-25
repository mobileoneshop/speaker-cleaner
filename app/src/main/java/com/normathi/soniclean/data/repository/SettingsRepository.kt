package com.normathi.soniclean.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.normathi.soniclean.data.theme.AppThemeMode
import com.normathi.soniclean.data.theme.DataStoreThemeManager
import com.normathi.soniclean.data.theme.ThemeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UserSettings(
    val theme: String = "system",
    val hapticsEnabled: Boolean = true,
    val safetyReminders: Boolean = true,
    val defaultOutput: String = "speaker",
    val disclaimerAccepted: Boolean = false,
    val manualLastFreq: Int = 180,
    val quickCleanDurationSec: Int = 15
)

interface SettingsRepository {
    val settings: StateFlow<UserSettings>
    fun setTheme(theme: String)
    fun setHapticsEnabled(enabled: Boolean)
    fun setSafetyReminders(enabled: Boolean)
    fun setDefaultOutput(output: String)
    fun setDisclaimerAccepted(accepted: Boolean)
    fun setManualLastFreq(freq: Int)
    fun setQuickCleanDurationSec(seconds: Int)
}

class DefaultSettingsRepository(
    context: Context,
    private val themeManager: ThemeManager = DataStoreThemeManager.getInstance(context),
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) : SettingsRepository {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _settings = MutableStateFlow(loadSettings())
    override val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    init {
        scope.launch {
            themeManager.themeMode.collect { mode ->
                if (_settings.value.theme != mode.storageKey) {
                    _settings.value = _settings.value.copy(theme = mode.storageKey)
                }
            }
        }
    }

    private fun loadSettings(): UserSettings {
        val currentThemeKey = themeManager.themeMode.value.storageKey
        return UserSettings(
            theme = currentThemeKey,
            hapticsEnabled = prefs.getBoolean(KEY_HAPTICS, true),
            safetyReminders = prefs.getBoolean(KEY_SAFETY_REMINDERS, true),
            defaultOutput = prefs.getString(KEY_DEFAULT_OUTPUT, "speaker") ?: "speaker",
            disclaimerAccepted = prefs.getBoolean(KEY_DISCLAIMER_ACCEPTED, false),
            manualLastFreq = prefs.getInt(KEY_MANUAL_LAST_FREQ, 180),
            quickCleanDurationSec = prefs.getInt(KEY_QUICK_CLEAN_DURATION, 15)
        )
    }

    override fun setTheme(theme: String) {
        val mode = AppThemeMode.fromKey(theme)
        themeManager.setThemeMode(mode)
        prefs.edit().putString(KEY_THEME, mode.storageKey).apply()
        _settings.value = _settings.value.copy(theme = mode.storageKey)
    }

    override fun setHapticsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTICS, enabled).apply()
        _settings.value = _settings.value.copy(hapticsEnabled = enabled)
    }

    override fun setSafetyReminders(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SAFETY_REMINDERS, enabled).apply()
        _settings.value = _settings.value.copy(safetyReminders = enabled)
    }

    override fun setDefaultOutput(output: String) {
        prefs.edit().putString(KEY_DEFAULT_OUTPUT, output).apply()
        _settings.value = _settings.value.copy(defaultOutput = output)
    }

    override fun setDisclaimerAccepted(accepted: Boolean) {
        prefs.edit().putBoolean(KEY_DISCLAIMER_ACCEPTED, accepted).apply()
        _settings.value = _settings.value.copy(disclaimerAccepted = accepted)
    }

    override fun setManualLastFreq(freq: Int) {
        prefs.edit().putInt(KEY_MANUAL_LAST_FREQ, freq).apply()
        _settings.value = _settings.value.copy(manualLastFreq = freq)
    }

    override fun setQuickCleanDurationSec(seconds: Int) {
        val clamped = seconds.coerceIn(5, 60)
        prefs.edit().putInt(KEY_QUICK_CLEAN_DURATION, clamped).apply()
        _settings.value = _settings.value.copy(quickCleanDurationSec = clamped)
    }

    companion object {
        private const val PREFS_NAME = "sonic_clean_settings"
        private const val KEY_THEME = "theme"
        private const val KEY_HAPTICS = "haptics_enabled"
        private const val KEY_SAFETY_REMINDERS = "safety_reminders"
        private const val KEY_DEFAULT_OUTPUT = "default_output"
        private const val KEY_DISCLAIMER_ACCEPTED = "disclaimer_accepted"
        private const val KEY_MANUAL_LAST_FREQ = "manual_last_freq"
        private const val KEY_QUICK_CLEAN_DURATION = "quick_clean_duration"

        @Volatile
        private var instance: DefaultSettingsRepository? = null

        fun getInstance(context: Context): DefaultSettingsRepository {
            return instance ?: synchronized(this) {
                instance ?: DefaultSettingsRepository(context).also { instance = it }
            }
        }
    }
}
