package com.normathi.soniclean.data.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val Context.themePreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

enum class AppThemeMode(val storageKey: String, val displayName: String) {
    SYSTEM("system", "System Default"),
    LIGHT("light", "Light"),
    DARK("dark", "Dark");

    companion object {
        fun fromKey(key: String?): AppThemeMode =
            entries.firstOrNull { it.storageKey.equals(key, ignoreCase = true) } ?: SYSTEM
    }
}

interface ThemeManager {
    val themeMode: StateFlow<AppThemeMode>
    fun setThemeMode(mode: AppThemeMode)
    suspend fun setThemeModeSuspend(mode: AppThemeMode)
}

class DataStoreThemeManager(
    private val context: Context,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) : ThemeManager {

    private val dataStore = context.applicationContext.themePreferencesDataStore

    companion object {
        val KEY_THEME_MODE = stringPreferencesKey("app_theme_mode")

        @Volatile
        private var instance: DataStoreThemeManager? = null

        fun getInstance(context: Context): DataStoreThemeManager {
            return instance ?: synchronized(this) {
                instance ?: DataStoreThemeManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val themeFlow: Flow<AppThemeMode> = dataStore.data.map { preferences ->
        val rawKey = preferences[KEY_THEME_MODE]
        AppThemeMode.fromKey(rawKey)
    }

    override val themeMode: StateFlow<AppThemeMode> = themeFlow.stateIn(
        scope = externalScope,
        started = SharingStarted.Eagerly,
        initialValue = AppThemeMode.SYSTEM
    )

    override fun setThemeMode(mode: AppThemeMode) {
        externalScope.launch {
            setThemeModeSuspend(mode)
        }
    }

    override suspend fun setThemeModeSuspend(mode: AppThemeMode) {
        dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode.storageKey
        }
    }
}
