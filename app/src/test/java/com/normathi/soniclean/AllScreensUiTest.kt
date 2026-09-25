package com.normathi.soniclean

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import com.normathi.soniclean.data.repository.DefaultHistoryRepository
import com.normathi.soniclean.nav.SonicCleanNavGraph
import com.normathi.soniclean.ui.screens.AutoCleanScreen
import com.normathi.soniclean.ui.screens.HistoryScreen
import com.normathi.soniclean.ui.screens.HomeScreen
import com.normathi.soniclean.ui.screens.ManualScreen
import com.normathi.soniclean.ui.screens.SettingsScreen
import com.normathi.soniclean.ui.screens.SoundTestScreen
import com.normathi.soniclean.ui.screens.SplashScreen
import com.normathi.soniclean.ui.screens.VibrationScreen
import com.normathi.soniclean.ui.screens.WaterEjectScreen
import com.normathi.soniclean.ui.theme.SonicCleanTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AllScreensUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        DefaultHistoryRepository.getInstance(app).clearAll()
    }

    @Test
    fun splash_screen_renders() {
        composeTestRule.setContent {
            SonicCleanTheme {
                SplashScreen(onSplashFinished = {})
            }
        }
        composeTestRule.onNodeWithTag("splash_screen").assertExists()
    }

    @Test
    fun home_screen_renders_and_has_all_nav_targets() {
        composeTestRule.setContent {
            SonicCleanTheme {
                HomeScreen()
            }
        }
        composeTestRule.onNodeWithTag("home_screen").assertExists()
        composeTestRule.onNodeWithTag("hero_auto_clean_card").assertExists()
        composeTestRule.onNodeWithTag("history_button").assertExists()
        composeTestRule.onNodeWithTag("settings_button").assertExists()

        composeTestRule.onNodeWithTag("home_lazy_column")
            .performScrollToNode(hasTestTag("quick_clean_button"))
        composeTestRule.onNodeWithTag("quick_clean_button").assertExists()

        composeTestRule.onNodeWithTag("home_lazy_column")
            .performScrollToNode(hasTestTag("mode_manual_card"))
        composeTestRule.onNodeWithTag("mode_manual_card").assertExists()

        composeTestRule.onNodeWithTag("home_lazy_column")
            .performScrollToNode(hasTestTag("mode_vibration_card"))
        composeTestRule.onNodeWithTag("mode_vibration_card").assertExists()

        composeTestRule.onNodeWithTag("home_lazy_column")
            .performScrollToNode(hasTestTag("mode_test_card"))
        composeTestRule.onNodeWithTag("mode_test_card").assertExists()
    }

    @Test
    fun auto_clean_screen_renders() {
        composeTestRule.setContent {
            SonicCleanTheme {
                AutoCleanScreen(onNavigateBack = {})
            }
        }
        composeTestRule.onNodeWithTag("auto_clean_screen").assertExists()
        composeTestRule.onNodeWithTag("clean_button").assertExists()
    }

    @Test
    fun manual_screen_renders() {
        composeTestRule.setContent {
            SonicCleanTheme {
                ManualScreen()
            }
        }
        composeTestRule.onNodeWithTag("manual_screen").assertExists()
        composeTestRule.onNodeWithTag("frequency_readout").assertExists()
        composeTestRule.onNodeWithTag("frequency_slider").assertExists()
        composeTestRule.onNodeWithTag("manual_play_button").assertExists()
    }

    @Test
    fun vibration_screen_renders() {
        composeTestRule.setContent {
            SonicCleanTheme {
                VibrationScreen()
            }
        }
        composeTestRule.onNodeWithTag("vibration_screen").assertExists()
        composeTestRule.onNodeWithTag("pattern_gentle").assertExists()
        composeTestRule.onNodeWithTag("pattern_deep").assertExists()
        composeTestRule.onNodeWithTag("pattern_pulse").assertExists()
        composeTestRule.onNodeWithTag("vibration_play_button").assertExists()
    }

    @Test
    fun sound_test_screen_renders() {
        composeTestRule.setContent {
            SonicCleanTheme {
                SoundTestScreen()
            }
        }
        composeTestRule.onNodeWithTag("sound_test_screen").assertExists()
        composeTestRule.onNodeWithTag("sound_test_play_button").assertExists()
    }

    @Test
    fun history_screen_renders() {
        composeTestRule.setContent {
            SonicCleanTheme {
                HistoryScreen()
            }
        }
        composeTestRule.onNodeWithTag("history_screen").assertExists()
        composeTestRule.onNodeWithTag("empty_history_start_button").assertExists()
    }

    @Test
    fun settings_screen_renders() {
        composeTestRule.setContent {
            SonicCleanTheme {
                SettingsScreen()
            }
        }
        composeTestRule.onNodeWithTag("settings_screen").assertExists()
        composeTestRule.onNodeWithTag("setting_theme_row").assertExists()
        composeTestRule.onNodeWithTag("setting_output_row").assertExists()
        composeTestRule.onNodeWithTag("setting_haptics_switch").assertExists()
        composeTestRule.onNodeWithTag("setting_safety_reminders_switch").assertExists()
        composeTestRule.onNodeWithTag("setting_about_row").assertExists()
        composeTestRule.onNodeWithTag("setting_privacy_row").assertExists()
        composeTestRule.onNodeWithTag("settings_version_text").assertExists()
    }

    @Test
    fun navgraph_can_navigate_from_home_to_manual_and_back() {
        composeTestRule.setContent {
            SonicCleanTheme {
                SonicCleanNavGraph(startDestination = "home")
            }
        }
        composeTestRule.onNodeWithTag("home_screen").assertExists()
        composeTestRule.onNodeWithTag("home_lazy_column")
            .performScrollToNode(hasTestTag("mode_manual_card"))
        composeTestRule.onNodeWithTag("mode_manual_card").performClick()
        composeTestRule.onNodeWithTag("manual_screen").assertExists()
        composeTestRule.onNodeWithTag("top_bar_back_button").performClick()
        composeTestRule.onNodeWithTag("home_screen").assertExists()
    }

    @Test
    fun navgraph_can_navigate_from_home_to_vibration_and_back() {
        composeTestRule.setContent {
            SonicCleanTheme {
                SonicCleanNavGraph(startDestination = "home")
            }
        }
        composeTestRule.onNodeWithTag("home_screen").assertExists()
        composeTestRule.onNodeWithTag("home_lazy_column")
            .performScrollToNode(hasTestTag("mode_vibration_card"))
        composeTestRule.onNodeWithTag("mode_vibration_card").performClick()
        composeTestRule.onNodeWithTag("vibration_screen").assertExists()
        composeTestRule.onNodeWithTag("top_bar_back_button").performClick()
        composeTestRule.onNodeWithTag("home_screen").assertExists()
    }

    @Test
    fun navgraph_can_navigate_from_home_to_sound_test_and_back() {
        composeTestRule.setContent {
            SonicCleanTheme {
                SonicCleanNavGraph(startDestination = "home")
            }
        }
        composeTestRule.onNodeWithTag("home_screen").assertExists()
        composeTestRule.onNodeWithTag("home_lazy_column")
            .performScrollToNode(hasTestTag("mode_test_card"))
        composeTestRule.onNodeWithTag("mode_test_card").performClick()
        composeTestRule.onNodeWithTag("sound_test_screen").assertExists()
        composeTestRule.onNodeWithTag("top_bar_back_button").performClick()
        composeTestRule.onNodeWithTag("home_screen").assertExists()
    }

    @Test
    fun navgraph_can_navigate_from_home_to_history_and_back() {
        composeTestRule.setContent {
            SonicCleanTheme {
                SonicCleanNavGraph(startDestination = "home")
            }
        }
        composeTestRule.onNodeWithTag("home_screen").assertExists()
        composeTestRule.onNodeWithTag("history_button").performClick()
        composeTestRule.onNodeWithTag("history_screen").assertExists()
        composeTestRule.onNodeWithTag("top_bar_back_button").performClick()
        composeTestRule.onNodeWithTag("home_screen").assertExists()
    }

    @Test
    fun navgraph_can_navigate_from_home_to_settings_and_back() {
        composeTestRule.setContent {
            SonicCleanTheme {
                SonicCleanNavGraph(startDestination = "home")
            }
        }
        composeTestRule.onNodeWithTag("home_screen").assertExists()
        composeTestRule.onNodeWithTag("settings_button").performClick()
        composeTestRule.onNodeWithTag("settings_screen").assertExists()
        composeTestRule.onNodeWithTag("top_bar_back_button").performClick()
        composeTestRule.onNodeWithTag("home_screen").assertExists()
    }

    @Test
    fun water_eject_screen_renders() {
        composeTestRule.setContent {
            SonicCleanTheme {
                WaterEjectScreen()
            }
        }
        composeTestRule.onNodeWithTag("water_eject_screen").assertExists()
        composeTestRule.onNodeWithTag("eject_water_button").assertExists()
        composeTestRule.onNodeWithTag("frequency_slider").assertExists()
    }

    @Test
    fun navgraph_can_navigate_from_home_to_water_eject_and_back() {
        composeTestRule.setContent {
            SonicCleanTheme {
                SonicCleanNavGraph(startDestination = "home")
            }
        }
        composeTestRule.onNodeWithTag("home_screen").assertExists()
        composeTestRule.onNodeWithTag("home_lazy_column")
            .performScrollToNode(hasTestTag("mode_water_eject_card"))
        composeTestRule.onNodeWithTag("mode_water_eject_card").performClick()
        composeTestRule.onNodeWithTag("water_eject_screen").assertExists()
        composeTestRule.onNodeWithTag("top_bar_back_button").performClick()
        composeTestRule.onNodeWithTag("home_screen").assertExists()
    }
}
