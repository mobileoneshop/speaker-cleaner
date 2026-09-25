package com.normathi.soniclean

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.normathi.soniclean.data.model.CleanSession
import com.normathi.soniclean.data.repository.DefaultHistoryRepository
import com.normathi.soniclean.engine.audio.AudioPlayer
import com.normathi.soniclean.engine.audio.AudioPlayerState
import com.normathi.soniclean.viewmodel.HomeViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

class FakeAudioPlayer : AudioPlayer {
    private val _state = MutableStateFlow<AudioPlayerState>(AudioPlayerState.Idle)
    override val state: StateFlow<AudioPlayerState> = _state
    override var isPlaying: Boolean = false
    override var currentFrequency: Float = 0f

    override fun play(frequencyHz: Float): Boolean {
        isPlaying = true
        currentFrequency = frequencyHz
        _state.value = AudioPlayerState.Playing(frequencyHz)
        return true
    }

    override fun updateFrequency(frequencyHz: Float) {
        currentFrequency = frequencyHz
    }

    override fun setVolume(volume: Float) {}

    override fun stop() {
        isPlaying = false
        _state.value = AudioPlayerState.Idle
    }

    override fun release() {
        stop()
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HomeViewModelTest {

    @Test
    fun factory_can_instantiate_homeviewmodel_with_application_only() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val constructor = HomeViewModel::class.java.getConstructor(Application::class.java)
        assertNotNull(constructor)
        val vm = constructor.newInstance(app)
        assertNotNull(vm)
    }

    @Test
    fun empty_history_shows_zero_and_dash_not_blank() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val repo = DefaultHistoryRepository(app)
        repo.clearAll()

        val viewModel = HomeViewModel(
            application = app,
            historyRepository = repo,
            audioPlayer = FakeAudioPlayer()
        )

        val state = viewModel.uiState.value
        assertEquals("0", state.totalCleans)
        assertEquals("0", state.thisWeekCleans)
        assertEquals("—", state.lastCleaned)
        assertFalse(state.isQuickCleaning)
        assertNull(state.headphoneWarning)
    }

    @Test
    fun populated_history_updates_stats_correctly() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val repo = DefaultHistoryRepository(app)
        repo.clearAll()

        repo.addSession(
            CleanSession(
                mode = "auto",
                durationSec = 34,
                peakFreqHz = 200,
                output = "speaker",
                timestamp = System.currentTimeMillis()
            )
        )

        val viewModel = HomeViewModel(
            application = app,
            historyRepository = repo,
            audioPlayer = FakeAudioPlayer()
        )

        val state = viewModel.uiState.value
        assertEquals("1", state.totalCleans)
        assertEquals("1", state.thisWeekCleans)
        assertEquals("Today", state.lastCleaned)
    }

    @Test
    fun quick_clean_tapped_starts_cleaning_and_can_be_stopped() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val repo = DefaultHistoryRepository(app)
        repo.clearAll()
        val fakeAudio = FakeAudioPlayer()

        val viewModel = HomeViewModel(
            application = app,
            historyRepository = repo,
            audioPlayer = fakeAudio
        )

        assertFalse(viewModel.uiState.value.isQuickCleaning)

        viewModel.onQuickCleanTapped()

        assertTrue(viewModel.uiState.value.isQuickCleaning)
        assertTrue(fakeAudio.isPlaying)
        assertEquals(15, viewModel.uiState.value.quickCleanRemainingSec)
        assertEquals(10f, fakeAudio.currentFrequency, 0.001f)

        viewModel.stopQuickClean()

        assertFalse(viewModel.uiState.value.isQuickCleaning)
        assertFalse(fakeAudio.isPlaying)
    }
}
