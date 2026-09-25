package com.normathi.soniclean.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.normathi.soniclean.R
import com.normathi.soniclean.data.model.CleanSession
import com.normathi.soniclean.data.repository.DefaultHistoryRepository
import com.normathi.soniclean.data.repository.HistoryRepository
import com.normathi.soniclean.engine.audio.AudioEngine
import com.normathi.soniclean.engine.audio.AudioOutputRouter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WaterEjectUiState(
    val frequencyHz: Float = AudioEngine.CALIBRATED_WATER_EJECT_FREQ,
    val minFrequencyHz: Float = 50.0f,
    val maxFrequencyHz: Float = 1000.0f,
    val isPlaying: Boolean = false,
    val durationLimitSeconds: Int = 30,
    val remainingSeconds: Int = 30,
    val elapsedSeconds: Int = 0,
    val headphoneWarning: String? = null,
    val isPulsedMode: Boolean = false,
    val isCompleteNotice: Boolean = false
) {
    val countdownProgress: Float
        get() = if (durationLimitSeconds > 0) {
            (1.0f - (remainingSeconds.toFloat() / durationLimitSeconds.toFloat())).coerceIn(0f, 1f)
        } else {
            0f
        }
}

class WaterEjectViewModel @JvmOverloads constructor(
    application: Application,
    val audioEngine: AudioEngine = AudioEngine(application),
    private val outputRouter: AudioOutputRouter = AudioOutputRouter(application),
    private val historyRepository: HistoryRepository = DefaultHistoryRepository.getInstance(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(WaterEjectUiState())
    val uiState: StateFlow<WaterEjectUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    init {
        outputRouter.routeToSpeaker()
        viewModelScope.launch {
            audioEngine.state.collect {
                val playing = audioEngine.isPlaying
                _uiState.update { it.copy(isPlaying = playing) }
            }
        }
    }

    /**
     * Updates the frequency when the slider moves.
     * Propagates changes to the running AudioEngine in real time.
     */
    fun onFrequencyChanged(newFreq: Float) {
        val clamped = newFreq.coerceIn(_uiState.value.minFrequencyHz, _uiState.value.maxFrequencyHz)
        _uiState.update { it.copy(frequencyHz = clamped) }
        if (audioEngine.isPlaying) {
            audioEngine.updateFrequency(clamped)
        }
    }

    /**
     * Sets the countdown timer duration limit (e.g. 15s, 30s, 45s, 60s)
     * to protect against excessive battery drain and voice-coil heating.
     */
    fun onDurationLimitChanged(seconds: Int) {
        val clamped = seconds.coerceIn(5, 60)
        _uiState.update {
            it.copy(
                durationLimitSeconds = clamped,
                remainingSeconds = if (it.isPlaying) it.remainingSeconds.coerceAtMost(clamped) else clamped
            )
        }
    }

    /**
     * Toggles water ejection playback.
     * Explicitly binds to [AudioEngine.playFrequency].
     */
    fun onEjectWaterClicked() {
        if (_uiState.value.isPlaying) {
            stopEjection(recordHistory = true)
        } else {
            startEjection()
        }
    }

    /**
     * Starts water ejection by routing to the loudspeaker and calling
     * [AudioEngine.playFrequency] with the selected calibrated frequency.
     * Manages countdown timer to automatically limit duration and protect battery.
     */
    fun startEjection() {
        // Safety: verify no headphones connected
        if (outputRouter.isHeadphonesConnected()) {
            _uiState.update {
                it.copy(
                    headphoneWarning = getApplication<Application>().getString(R.string.safety_headphones)
                )
            }
            return
        }

        val limit = _uiState.value.durationLimitSeconds
        _uiState.update {
            it.copy(
                headphoneWarning = null,
                elapsedSeconds = 0,
                remainingSeconds = limit,
                isCompleteNotice = false
            )
        }
        outputRouter.routeToSpeaker()

        val freqToPlay = _uiState.value.frequencyHz
        // Direct invocation of AudioEngine's playFrequency method
        val success = audioEngine.playFrequency(freqToPlay)
        if (!success) return

        _uiState.update { it.copy(isPlaying = true) }

        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            var timeLeft = limit
            while (_uiState.value.isPlaying && timeLeft > 0) {
                delay(1000L)
                if (outputRouter.isHeadphonesConnected()) {
                    stopEjection(recordHistory = true)
                    _uiState.update {
                        it.copy(
                            headphoneWarning = getApplication<Application>().getString(R.string.safety_stopped_headphones)
                        )
                    }
                    return@launch
                }
                timeLeft--
                val elapsed = limit - timeLeft
                _uiState.update {
                    it.copy(
                        remainingSeconds = timeLeft,
                        elapsedSeconds = elapsed
                    )
                }
            }

            // Auto-stop when countdown reaches 0 to conserve battery
            if (_uiState.value.isPlaying) {
                stopEjection(recordHistory = true)
                _uiState.update {
                    it.copy(
                        isCompleteNotice = true,
                        remainingSeconds = it.durationLimitSeconds
                    )
                }
            }
        }
    }

    /**
     * Stops water ejection and safely records history if ran for >= 3 seconds.
     */
    fun stopEjection(recordHistory: Boolean = false) {
        val elapsed = _uiState.value.elapsedSeconds
        val freq = _uiState.value.frequencyHz.toInt()

        countdownJob?.cancel()
        countdownJob = null
        audioEngine.stop()
        outputRouter.restore()

        _uiState.update {
            it.copy(
                isPlaying = false,
                remainingSeconds = it.durationLimitSeconds
            )
        }

        if (recordHistory && elapsed >= 3) {
            historyRepository.addSession(
                CleanSession(
                    mode = "water_eject",
                    durationSec = elapsed,
                    peakFreqHz = freq,
                    output = "speaker"
                )
            )
        }
    }

    fun dismissHeadphoneWarning() {
        _uiState.update { it.copy(headphoneWarning = null) }
    }

    fun dismissCompleteNotice() {
        _uiState.update { it.copy(isCompleteNotice = false) }
    }

    override fun onCleared() {
        super.onCleared()
        stopEjection(recordHistory = false)
        audioEngine.release()
    }
}
