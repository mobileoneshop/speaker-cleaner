package com.normathi.soniclean

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.normathi.soniclean.engine.audio.AudioEngine
import com.normathi.soniclean.ui.screens.WaterEjectContent
import com.normathi.soniclean.ui.screens.WaterEjectScreen
import com.normathi.soniclean.ui.theme.SonicCleanTheme
import com.normathi.soniclean.viewmodel.WaterEjectViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WaterEjectScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    class TestAudioEngine(application: Application) : AudioEngine(application) {
        var lastPlayedFrequency: Float = -1f
        var playFrequencyCallCount: Int = 0

        override fun play(frequencyHz: Float): Boolean {
            lastPlayedFrequency = frequencyHz
            playFrequencyCallCount++
            return true
        }

        override fun playFrequency(frequencyHz: Float): Boolean {
            return play(frequencyHz)
        }
    }

    @Test
    fun waterEjectScreen_rendersKeyElementsAndCountdownTimer() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val testEngine = TestAudioEngine(app)
        val viewModel = WaterEjectViewModel(application = app, audioEngine = testEngine)

        composeTestRule.setContent {
            SonicCleanTheme {
                WaterEjectScreen(viewModel = viewModel)
            }
        }

        // Central Eject Water button
        composeTestRule.onNodeWithTag("eject_water_button").assertExists()

        // Frequency Adjustment Slider & Readout
        composeTestRule.onNodeWithTag("frequency_slider").assertExists()
        composeTestRule.onNodeWithTag("water_eject_freq_readout").assertExists()

        // Countdown Timer & Battery Saver Card
        composeTestRule.onNodeWithTag("countdown_timer_card").assertExists()
        composeTestRule.onNodeWithTag("duration_chip_15").assertExists()
        composeTestRule.onNodeWithTag("duration_chip_30").assertExists()
        composeTestRule.onNodeWithTag("duration_chip_60").assertExists()

        // Preset chips
        composeTestRule.onNodeWithTag("water_preset_chip_165").assertExists()
        composeTestRule.onNodeWithTag("water_preset_chip_180").assertExists()
    }

    @Test
    fun waterEjectViewModel_sliderAdjustsBetween50HzAnd1000Hz() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val testEngine = TestAudioEngine(app)
        val viewModel = WaterEjectViewModel(application = app, audioEngine = testEngine)

        assertEquals(165.0f, viewModel.uiState.value.frequencyHz, 0.001f)

        viewModel.onFrequencyChanged(180.0f)
        assertEquals(180.0f, viewModel.uiState.value.frequencyHz, 0.001f)

        // Clamping below 50Hz
        viewModel.onFrequencyChanged(20.0f)
        assertEquals(50.0f, viewModel.uiState.value.frequencyHz, 0.001f)

        // Clamping above 1000Hz
        viewModel.onFrequencyChanged(1500.0f)
        assertEquals(1000.0f, viewModel.uiState.value.frequencyHz, 0.001f)
    }

    @Test
    fun waterEjectViewModel_countdownDurationLimitConfiguration() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val testEngine = TestAudioEngine(app)
        val viewModel = WaterEjectViewModel(application = app, audioEngine = testEngine)

        assertEquals(30, viewModel.uiState.value.durationLimitSeconds)
        assertEquals(30, viewModel.uiState.value.remainingSeconds)

        viewModel.onDurationLimitChanged(15)
        assertEquals(15, viewModel.uiState.value.durationLimitSeconds)
        assertEquals(15, viewModel.uiState.value.remainingSeconds)

        viewModel.onDurationLimitChanged(60)
        assertEquals(60, viewModel.uiState.value.durationLimitSeconds)
    }

    @Test
    fun waterEjectViewModel_bindsButtonToAudioEnginePlayFrequency() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val testEngine = TestAudioEngine(app)
        val viewModel = WaterEjectViewModel(application = app, audioEngine = testEngine)

        viewModel.onFrequencyChanged(165.0f)
        viewModel.onEjectWaterClicked()

        assertEquals(1, testEngine.playFrequencyCallCount)
        assertEquals(165.0f, testEngine.lastPlayedFrequency, 0.001f)
        assertTrue(viewModel.uiState.value.isPlaying)

        // Toggling stops
        viewModel.onEjectWaterClicked()
        assertEquals(false, viewModel.uiState.value.isPlaying)
    }

    @Test
    fun waterEjectContent_directAudioEngineBinding() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val testEngine = TestAudioEngine(app)

        composeTestRule.setContent {
            SonicCleanTheme {
                WaterEjectContent(audioEngine = testEngine)
            }
        }

        composeTestRule.onNodeWithTag("eject_water_button").assertExists()
        composeTestRule.onNodeWithTag("eject_water_button").performClick()

        assertEquals(1, testEngine.playFrequencyCallCount)
        assertEquals(165.0f, testEngine.lastPlayedFrequency, 0.001f)
    }
}
