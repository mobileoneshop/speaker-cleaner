package com.normathi.soniclean.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.normathi.soniclean.R
import com.normathi.soniclean.data.model.CleanSession
import com.normathi.soniclean.data.repository.DefaultHistoryRepository
import com.normathi.soniclean.data.repository.DefaultSettingsRepository
import com.normathi.soniclean.data.repository.HistoryRepository
import com.normathi.soniclean.data.repository.SettingsRepository
import com.normathi.soniclean.engine.audio.AndroidAudioPlayer
import com.normathi.soniclean.engine.audio.AudioConfig
import com.normathi.soniclean.engine.audio.AudioOutputRouter
import com.normathi.soniclean.engine.audio.AudioOutputTarget
import com.normathi.soniclean.engine.audio.AudioPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ManualUiState(
    val frequencyHz: Int = AudioConfig.DEFAULT_MANUAL_FREQ,
    val isPlaying: Boolean = false,
    val elapsedSeconds: Int = 0,
    val selectedDurationSeconds: Int = 30,
    val maxDurationSeconds: Int = 30,
    val outputTarget: AudioOutputTarget = AudioOutputTarget.SPEAKER,
    val headphoneWarning: String? = null,
    val noticeMessage: String? = null
) {
    val remainingSeconds: Int
        get() = (maxDurationSeconds - elapsedSeconds).coerceAtLeast(0)

    val progress: Float
        get() = if (maxDurationSeconds > 0) {
            (elapsedSeconds.toFloat() / maxDurationSeconds.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
}

class ManualViewModel @JvmOverloads constructor(
    application: Application,
    private val audioPlayer: AudioPlayer = AndroidAudioPlayer(application),
    private val outputRouter: AudioOutputRouter = AudioOutputRouter(application),
    private val historyRepository: HistoryRepository = DefaultHistoryRepository.getInstance(application),
    private val settingsRepository: SettingsRepository = DefaultSettingsRepository.getInstance(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(
        ManualUiState(
            frequencyHz = settingsRepository.settings.value.manualLastFreq
        )
    )
    val uiState: StateFlow<ManualUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        outputRouter.routeToSpeaker()
    }

    fun onFrequencyChanged(newFreq: Int) {
        val clamped = newFreq.coerceIn(AudioConfig.MIN_FREQ, AudioConfig.MAX_FREQ)
        _uiState.update { it.copy(frequencyHz = clamped) }
        settingsRepository.setManualLastFreq(clamped)
        if (_uiState.value.isPlaying) {
            audioPlayer.updateFrequency(clamped.toFloat())
        }
    }

    fun onDurationChanged(seconds: Int) {
        val clamped = seconds.coerceIn(5, 300)
        _uiState.update {
            it.copy(
                selectedDurationSeconds = clamped,
                maxDurationSeconds = clamped,
                elapsedSeconds = 0
            )
        }
    }

    fun onTogglePlay() {
        if (_uiState.value.isPlaying) {
            stopPlaying(recordHistory = true)
        } else {
            startPlaying()
        }
    }

    private fun startPlaying() {
        // Headphone check
        if (outputRouter.isHeadphonesConnected()) {
            _uiState.update {
                it.copy(
                    headphoneWarning = getApplication<Application>().getString(R.string.safety_headphones)
                )
            }
            return
        }

        val maxSec = if (_uiState.value.outputTarget == AudioOutputTarget.EARPIECE) {
            AudioConfig.EARPIECE_BURST_MAX_SEC
        } else {
            _uiState.value.selectedDurationSeconds
        }

        _uiState.update {
            it.copy(
                headphoneWarning = null,
                noticeMessage = null,
                elapsedSeconds = 0,
                maxDurationSeconds = maxSec
            )
        }

        if (_uiState.value.outputTarget == AudioOutputTarget.EARPIECE) {
            outputRouter.routeToEarpiece()
        } else {
            outputRouter.routeToSpeaker()
        }

        val started = audioPlayer.play(_uiState.value.frequencyHz.toFloat())
        if (!started) return

        _uiState.update { it.copy(isPlaying = true) }

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.isPlaying && _uiState.value.elapsedSeconds < maxSec) {
                delay(1000L)
                if (outputRouter.isHeadphonesConnected()) {
                    stopPlaying(recordHistory = true)
                    _uiState.update {
                        it.copy(
                            headphoneWarning = getApplication<Application>().getString(R.string.safety_stopped_headphones)
                        )
                    }
                    return@launch
                }
                _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }

            // Auto-stopped at max duration
            if (_uiState.value.isPlaying) {
                stopPlaying(recordHistory = true)
                _uiState.update {
                    it.copy(
                        noticeMessage = getApplication<Application>().getString(R.string.manual_autostop)
                    )
                }
            }
        }
    }

    fun onToggleOutputTarget(target: AudioOutputTarget) {
        _uiState.update { it.copy(outputTarget = target) }
        if (target == AudioOutputTarget.EARPIECE) {
            outputRouter.routeToEarpiece()
        } else {
            outputRouter.routeToSpeaker()
        }
    }

    fun stopPlaying(recordHistory: Boolean = false) {
        timerJob?.cancel()
        timerJob = null
        val wasPlaying = _uiState.value.isPlaying
        audioPlayer.stop()
        val durationSec = _uiState.value.elapsedSeconds

        _uiState.update {
            it.copy(
                isPlaying = false,
                elapsedSeconds = 0
            )
        }

        if (recordHistory && wasPlaying && durationSec > 0) {
            viewModelScope.launch {
                val session = CleanSession(
                    mode = "manual",
                    durationSec = durationSec,
                    peakFreqHz = _uiState.value.frequencyHz,
                    output = if (_uiState.value.outputTarget == AudioOutputTarget.EARPIECE) "earpiece" else "speaker"
                )
                historyRepository.addSession(session)
            }
        }
    }

    fun dismissHeadphoneWarning() {
        _uiState.update { it.copy(headphoneWarning = null) }
    }

    fun dismissNotice() {
        _uiState.update { it.copy(noticeMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        stopPlaying(recordHistory = false)
        audioPlayer.release()
    }
}
