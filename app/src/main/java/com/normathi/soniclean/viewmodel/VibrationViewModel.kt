package com.normathi.soniclean.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.normathi.soniclean.data.model.CleanSession
import com.normathi.soniclean.data.repository.DefaultHistoryRepository
import com.normathi.soniclean.data.repository.HistoryRepository
import com.normathi.soniclean.engine.vibration.VibrationController
import com.normathi.soniclean.engine.vibration.VibrationEngine
import com.normathi.soniclean.engine.vibration.VibrationPattern
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VibrationUiState(
    val selectedPattern: VibrationPattern = VibrationPattern.GENTLE,
    val isVibrating: Boolean = false,
    val elapsedSeconds: Int = 0,
    val totalSeconds: Int = 30,
    val isSupported: Boolean = true
)

class VibrationViewModel @JvmOverloads constructor(
    application: Application,
    private val vibrationController: VibrationController = VibrationEngine(application),
    private val historyRepository: HistoryRepository = DefaultHistoryRepository.getInstance(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(
        VibrationUiState(isSupported = vibrationController.isSupported)
    )
    val uiState: StateFlow<VibrationUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun selectPattern(pattern: VibrationPattern) {
        if (_uiState.value.isVibrating) {
            stopVibration(recordHistory = true)
        }
        _uiState.update { it.copy(selectedPattern = pattern) }
    }

    fun onToggleVibration() {
        if (_uiState.value.isVibrating) {
            stopVibration(recordHistory = true)
        } else {
            startVibration()
        }
    }

    private fun startVibration() {
        if (!_uiState.value.isSupported) return

        vibrationController.startPattern(_uiState.value.selectedPattern)
        _uiState.update { it.copy(isVibrating = true, elapsedSeconds = 0) }

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.isVibrating && _uiState.value.elapsedSeconds < _uiState.value.totalSeconds) {
                delay(1000L)
                _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }

            if (_uiState.value.isVibrating) {
                stopVibration(recordHistory = true)
            }
        }
    }

    fun stopVibration(recordHistory: Boolean = false) {
        val elapsed = _uiState.value.elapsedSeconds
        timerJob?.cancel()
        timerJob = null
        vibrationController.stop()
        _uiState.update { it.copy(isVibrating = false) }

        if (recordHistory && elapsed >= 3) {
            historyRepository.addSession(
                CleanSession(
                    mode = "vibration",
                    durationSec = elapsed,
                    peakFreqHz = null,
                    output = "vibration"
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopVibration(recordHistory = false)
    }
}
