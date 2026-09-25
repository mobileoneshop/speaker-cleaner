package com.normathi.soniclean

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.normathi.soniclean.data.theme.AppThemeMode
import com.normathi.soniclean.data.theme.DataStoreThemeManager
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ThemeManagerTest {

    @Test
    fun default_theme_is_system() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val themeManager = DataStoreThemeManager.getInstance(app)
        assertEquals(AppThemeMode.SYSTEM, themeManager.themeMode.value)
    }

    @Test
    fun app_theme_mode_enum_and_mapping() {
        assertEquals("system", AppThemeMode.SYSTEM.storageKey)
        assertEquals("light", AppThemeMode.LIGHT.storageKey)
        assertEquals("dark", AppThemeMode.DARK.storageKey)

        assertEquals("System Default", AppThemeMode.SYSTEM.displayName)
        assertEquals("Light", AppThemeMode.LIGHT.displayName)
        assertEquals("Dark", AppThemeMode.DARK.displayName)

        assertEquals(AppThemeMode.DARK, AppThemeMode.fromKey("dark"))
        assertEquals(AppThemeMode.LIGHT, AppThemeMode.fromKey("light"))
        assertEquals(AppThemeMode.SYSTEM, AppThemeMode.fromKey("system"))
        assertEquals(AppThemeMode.SYSTEM, AppThemeMode.fromKey("invalid"))
        assertEquals(AppThemeMode.SYSTEM, AppThemeMode.fromKey(null))
    }
}
