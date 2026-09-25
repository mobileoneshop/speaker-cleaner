package com.normathi.soniclean

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.normathi.soniclean.data.theme.AppThemeMode
import com.normathi.soniclean.nav.NavRoutes
import com.normathi.soniclean.nav.SonicCleanNavGraph
import com.normathi.soniclean.ui.theme.SonicCleanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val settingsRepository = com.normathi.soniclean.data.repository.DefaultSettingsRepository.getInstance(applicationContext)

        setContent {
            val settings by settingsRepository.settings.collectAsState()
            val isSystemDark = isSystemInDarkTheme()
            val darkTheme = when (settings.theme) {
                "dark" -> true
                "light" -> false
                else -> isSystemDark
            }

            val route = intent?.getStringExtra("route") ?: NavRoutes.HOME
            SonicCleanTheme(darkTheme = darkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SonicCleanNavGraph(startDestination = route)
                }
            }
        }
    }
}
