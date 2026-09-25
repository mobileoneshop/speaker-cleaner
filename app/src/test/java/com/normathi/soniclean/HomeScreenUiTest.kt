package com.normathi.soniclean

import android.app.Application
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import com.normathi.soniclean.data.repository.DefaultHistoryRepository
import com.normathi.soniclean.ui.screens.HomeScreen
import com.normathi.soniclean.ui.theme.SonicCleanTheme
import com.normathi.soniclean.viewmodel.HomeViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HomeScreenUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun home_screen_renders_all_components_top_to_bottom() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val repo = DefaultHistoryRepository(app)
        repo.clearAll()

        val viewModel = HomeViewModel(
            application = app,
            historyRepository = repo,
            audioPlayer = FakeAudioPlayer()
        )

        composeTestRule.setContent {
            SonicCleanTheme {
                HomeScreen(viewModel = viewModel)
            }
        }

        // 1. Header & Icons
        composeTestRule.onNodeWithTag("home_screen").assertExists()
        composeTestRule.onNodeWithTag("history_button").assertExists()
        composeTestRule.onNodeWithTag("settings_button").assertExists()

        // 2. Upgraded Auto Clean Hero Card with embedded Mini Waveform
        composeTestRule.onNodeWithTag("hero_auto_clean_card").assertExists()
        composeTestRule.onNodeWithTag("mini_waveform_visualizer", useUnmergedTree = true).assertExists()

        // 3. Stats Row
        composeTestRule.onNodeWithTag("home_stats_row").assertExists()
        composeTestRule.onNodeWithTag("stat_card_total").assertExists()
        composeTestRule.onNodeWithTag("stat_card_this_week").assertExists()
        composeTestRule.onNodeWithTag("stat_card_last_cleaned").assertExists()

        // 6. Quick Clean · 10s button
        composeTestRule.onNodeWithTag("home_lazy_column")
            .performScrollToNode(hasTestTag("quick_clean_button"))
        composeTestRule.onNodeWithTag("quick_clean_button").assertExists()

        // 4. Mode Cards (Water Eject, Manual, Vibration, Test)
        composeTestRule.onNodeWithTag("home_lazy_column")
            .performScrollToNode(hasTestTag("mode_water_eject_card"))
        composeTestRule.onNodeWithTag("mode_water_eject_card").assertExists()

        composeTestRule.onNodeWithTag("home_lazy_column")
            .performScrollToNode(hasTestTag("mode_manual_card"))
        composeTestRule.onNodeWithTag("mode_manual_card").assertExists()

        composeTestRule.onNodeWithTag("home_lazy_column")
            .performScrollToNode(hasTestTag("mode_vibration_card"))
        composeTestRule.onNodeWithTag("mode_vibration_card").assertExists()

        composeTestRule.onNodeWithTag("home_lazy_column")
            .performScrollToNode(hasTestTag("mode_test_card"))
        composeTestRule.onNodeWithTag("mode_test_card").assertExists()

        // 5. Tips Carousel Card
        composeTestRule.onNodeWithTag("home_lazy_column")
            .performScrollToNode(hasTestTag("tips_carousel_card"))
        composeTestRule.onNodeWithTag("tips_carousel_card").assertExists()
    }

    @Test
    fun home_screen_renders_with_default_viewmodel_factory() {
        composeTestRule.setContent {
            SonicCleanTheme {
                HomeScreen()
            }
        }
        composeTestRule.onNodeWithTag("home_screen").assertExists()
    }
}
