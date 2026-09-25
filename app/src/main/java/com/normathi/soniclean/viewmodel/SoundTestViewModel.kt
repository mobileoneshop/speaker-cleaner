package com.normathi.soniclean.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.normathi.soniclean.R
import com.normathi.soniclean.data.model.CleanSession
import com.normathi.soniclean.data.repository.DefaultHistoryRepository
import com.normathi.soniclean.data.repository.HistoryRepository
import com.normathi.soniclean.engine.audio.AndroidAudioPlayer
import com.normathi.soniclean.engine.audio.AudioConfig
import com.normathi.soniclean.engine.audio.AudioOutputRouter
import com.normathi.soniclean.engine.audio.AudioPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SoundTestUiState(
    val isPlaying: Boolean = false,
    val currentFrequencyHz: Float = AudioConfig.TEST_SWEEP_START_FREQ.toFloat(),
    val progress: Float = 0f,
    val isComplete: Boolean = false,
    val headphoneWarning: String? = null
)

class SoundTestViewModel @JvmOverloads constructor(
    application: Application,
    private val audioPlayer: AudioPlayer = AndroidAudioPlayer(application),
    private val outputRouter: AudioOutputRouter = AudioOutputRouter(application),
    private val historyRepository: HistoryRepository = DefaultHistoryRepository.getInstance(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SoundTestUiState())
    val uiState: StateFlow<SoundTestUiState> = _uiState.asStateFlow()

    private var sweepJob: Job? = null

    init {
        outputRouter.routeToSpeaker()
    }

    fun startTestSweep() {
        if (outputRouter.isHeadphonesConnected()) {
            _uiState.update {
                it.copy(headphoneWarning = getApplication<Application>().getString(R.string.safety_headphones))
            }
            return
        }

        outputRouter.routeToSpeaker()
        _uiState.update {
            it.copy(
                isPlaying = true,
                progress = 0f,
                isComplete = false,
                currentFrequencyHz = AudioConfig.TEST_SWEEP_START_FREQ.toFloat(),
                headphoneWarning = null
            )
        }

        audioPlayer.play(AudioConfig.TEST_SWEEP_START_FREQ.toFloat())

        sweepJob?.cancel()
        sweepJob = viewModelScope.launch {
            val durationMs = AudioConfig.TEST_SWEEP_DURATION_SEC * 1000L
            val stepMs = 50L
            var elapsedMs = 0L

            while (elapsedMs < durationMs) {
                delay(stepMs)
                elapsedMs += stepMs

                if (outputRouter.isHeadphonesConnected()) {
                    stop()
                    _uiState.update {
                        it.copy(headphoneWarning = getApplication<Application>().getString(R.string.safety_stopped_headphones))
                    }
                    return@launch
                }

                val ratio = (elapsedMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                val freq = AudioConfig.TEST_SWEEP_START_FREQ + ratio * (AudioConfig.TEST_SWEEP_END_FREQ - AudioConfig.TEST_SWEEP_START_FREQ)
                audioPlayer.updateFrequency(freq)
                _uiState.update {
                    it.copy(progress = ratio, currentFrequencyHz = freq)
                }
            }

            audioPlayer.stop()
            outputRouter.restore()

            historyRepository.addSession(
                CleanSession(
                    mode = "sound_test",
                    durationSec = AudioConfig.TEST_SWEEP_DURATION_SEC,
                    peakFreqHz = AudioConfig.TEST_SWEEP_END_FREQ,
                    output = "speaker"
                )
            )

            _uiState.update {
                it.copy(
                    isPlaying = false,
                    isComplete = true,
                    progress = 1f
                )
            }
        }
    }

    fun stop() {
        sweepJob?.cancel()
        sweepJob = null
        audioPlayer.stop()
        outputRouter.restore()
        _uiState.update { it.copy(isPlaying = false) }
    }

    fun dismissHeadphoneWarning() {
        _uiState.update { it.copy(headphoneWarning = null) }
    }

    override fun onCleared() {
        super.onCleared()
        stop()
        audioPlayer.release()
    }
}
