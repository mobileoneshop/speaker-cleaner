package com.normathi.soniclean.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.normathi.soniclean.engine.audio.AndroidAudioPlayer
import com.normathi.soniclean.engine.audio.AudioOutputRouter
import com.normathi.soniclean.engine.audio.AudioOutputTarget
import com.normathi.soniclean.engine.audio.AudioPlayer
import com.normathi.soniclean.engine.audio.SweepTable
import com.normathi.soniclean.engine.audio.VolumeManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AutoCleanUiState(
    val isCleaning: Boolean = false,
    val progress: Float = 0.0f,
    val currentStepNumber: Int = 1,
    val totalSteps: Int = 4,
    val currentStepTitle: String = "Ready to start",
    val currentFrequencyHz: Float = 165f,
    val elapsedSeconds: Float = 0.0f,
    val totalDurationSeconds: Float = SweepTable.TOTAL_AUTO_CYCLE_DURATION_SEC,
    val outputTarget: AudioOutputTarget = AudioOutputTarget.SPEAKER,
    val isVolumeMaximized: Boolean = false,
    val isCompleted: Boolean = false,
    val showHeadphoneWarning: Boolean = false,
    val showSafetyDialog: Boolean = false,
    val errorMessage: String? = null
)

class AutoCleanViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("sonic_clean_prefs", android.content.Context.MODE_PRIVATE)
    private val keySafetyAcknowledged = "safety_warning_acknowledged"

    private val audioPlayer: AudioPlayer = AndroidAudioPlayer(application)
    private val volumeManager = VolumeManager(application)
    private val outputRouter = AudioOutputRouter(application)

    private val _uiState = MutableStateFlow(AutoCleanUiState())
    val uiState: StateFlow<AutoCleanUiState> = _uiState.asStateFlow()

    private var cleaningJob: Job? = null

    init {
        outputRouter.routeToSpeaker()
    }

    /**
     * Entry point when user clicks the Clean Speaker button.
     * Evaluates headphone safety and prompts with Safety First warning if needed.
     */
    fun onCleanTapped() {
        if (_uiState.value.isCleaning) {
            stopCleaning()
            return
        }

        // Safety check: block if headphones are plugged in
        if (outputRouter.isHeadphonesConnected()) {
            _uiState.value = _uiState.value.copy(
                showHeadphoneWarning = true,
                errorMessage = "Disconnect headphones to protect your hearing."
            )
            return
        }

        val isAcknowledged = prefs.getBoolean(keySafetyAcknowledged, false)
        if (!isAcknowledged) {
            _uiState.value = _uiState.value.copy(showSafetyDialog = true)
        } else {
            startCleaning()
        }
    }

    fun onSafetyConfirmed(dontShowAgain: Boolean) {
        if (dontShowAgain) {
            prefs.edit().putBoolean(keySafetyAcknowledged, true).apply()
        }
        _uiState.value = _uiState.value.copy(showSafetyDialog = false)
        startCleaning()
    }

    fun onSafetyDismissed() {
        _uiState.value = _uiState.value.copy(showSafetyDialog = false)
    }

    fun showSafetyDialogManually() {
        _uiState.value = _uiState.value.copy(showSafetyDialog = true)
    }

    fun startCleaning() {
        if (_uiState.value.isCleaning) return

        // Safety check: block if headphones are plugged in
        if (outputRouter.isHeadphonesConnected()) {
            _uiState.value = _uiState.value.copy(
                showHeadphoneWarning = true,
                errorMessage = "Disconnect headphones to protect your hearing."
            )
            return
        }

        // Apply volume boost to max volume strictly for the ejection process
        val volumeBoosted = volumeManager.setMaxVolumeForEjection()

        _uiState.value = _uiState.value.copy(
            isCleaning = true,
            isCompleted = false,
            progress = 0f,
            elapsedSeconds = 0f,
            showHeadphoneWarning = false,
            errorMessage = null,
            isVolumeMaximized = volumeBoosted
        )

        cleaningJob?.cancel()
        cleaningJob = viewModelScope.launch {
            try {
                runEjectionSequence()
            } finally {
                // Ensure volume is always restored to previous level
                volumeManager.restoreVolume()
                audioPlayer.stop()
                _uiState.value = _uiState.value.copy(
                    isCleaning = false,
                    isVolumeMaximized = false
                )
            }
        }
    }

    private suspend fun runEjectionSequence() {
        val steps = SweepTable.AUTO_CYCLE
        val totalDuration = SweepTable.TOTAL_AUTO_CYCLE_DURATION_SEC
        var globalElapsed = 0.0f

        for (step in steps) {
            val stepDuration = step.durationSec
            var stepElapsed = 0.0f
            val updateIntervalMs = 50L

            if (step.isSilence) {
                audioPlayer.stop()
            } else {
                audioPlayer.play(step.startFreqHz)
            }

            while (stepElapsed < stepDuration) {
                // Check if headphones plugged in mid-session
                if (outputRouter.isHeadphonesConnected()) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Stopped — headphones detected.",
                        showHeadphoneWarning = true
                    )
                    return
                }

                val currentFreq = step.frequencyAt(stepElapsed)
                if (!step.isSilence) {
                    audioPlayer.updateFrequency(currentFreq)
                }

                globalElapsed += (updateIntervalMs / 1000f)
                stepElapsed += (updateIntervalMs / 1000f)

                val progressFraction = (globalElapsed / totalDuration).coerceIn(0f, 1f)

                _uiState.value = _uiState.value.copy(
                    progress = progressFraction,
                    currentStepNumber = step.stepIndex,
                    currentStepTitle = step.title,
                    currentFrequencyHz = currentFreq,
                    elapsedSeconds = globalElapsed
                )

                delay(updateIntervalMs)
            }
        }

        // Cycle successfully completed
        _uiState.value = _uiState.value.copy(
            isCleaning = false,
            progress = 1.0f,
            isCompleted = true,
            currentStepTitle = "Cleaning complete"
        )
    }

    fun stopCleaning() {
        cleaningJob?.cancel()
        cleaningJob = null
        audioPlayer.stop()
        volumeManager.restoreVolume()

        _uiState.value = _uiState.value.copy(
            isCleaning = false,
            isVolumeMaximized = false,
            currentStepTitle = "Cleaning stopped"
        )
    }

    fun dismissWarning() {
        _uiState.value = _uiState.value.copy(
            showHeadphoneWarning = false,
            errorMessage = null
        )
    }

    fun toggleOutputTarget() {
        if (_uiState.value.isCleaning) return
        val newTarget = if (_uiState.value.outputTarget == AudioOutputTarget.SPEAKER) {
            outputRouter.routeToEarpiece()
            AudioOutputTarget.EARPIECE
        } else {
            outputRouter.routeToSpeaker()
            AudioOutputTarget.SPEAKER
        }
        _uiState.value = _uiState.value.copy(outputTarget = newTarget)
    }

    fun resetState() {
        stopCleaning()
        _uiState.value = AutoCleanUiState(
            outputTarget = _uiState.value.outputTarget
        )
    }

    override fun onCleared() {
        super.onCleared()
        cleaningJob?.cancel()
        audioPlayer.release()
        volumeManager.restoreVolume()
        outputRouter.restore()
    }
}
