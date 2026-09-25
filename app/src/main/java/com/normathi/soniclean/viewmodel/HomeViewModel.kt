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
import com.normathi.soniclean.engine.audio.AudioOutputRouter
import com.normathi.soniclean.engine.audio.AudioPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

data class HomeUiState(
    val totalCleans: String = "—",
    val thisWeekCleans: String = "—",
    val lastCleaned: String = "—",
    val isQuickCleaning: Boolean = false,
    val quickCleanTotalDurationSec: Int = 15,
    val quickCleanRemainingSec: Int = 15,
    val currentQuickCleanFreq: Int = 10,
    val headphoneWarning: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class HomeViewModel @JvmOverloads constructor(
    application: Application,
    private val historyRepository: HistoryRepository = DefaultHistoryRepository.getInstance(application),
    private val settingsRepository: SettingsRepository = DefaultSettingsRepository.getInstance(application),
    private val audioPlayer: AudioPlayer = AndroidAudioPlayer(application),
    private val outputRouter: AudioOutputRouter = AudioOutputRouter(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var quickCleanJob: Job? = null

    init {
        observeHistory()
    }

    private fun observeHistory() {
        viewModelScope.launch {
            historyRepository.sessions.collect { sessions ->
                updateStats(sessions)
            }
        }
    }

    private fun updateStats(sessions: List<CleanSession>) {
        val app = getApplication<Application>()
        if (sessions.isEmpty()) {
            _uiState.update {
                it.copy(
                    totalCleans = "0",
                    thisWeekCleans = "0",
                    lastCleaned = app.getString(R.string.stat_empty_dash),
                    isLoading = false
                )
            }
            return
        }

        val total = sessions.size.toString()

        val startOfWeek = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val thisWeek = sessions.count { it.timestamp >= startOfWeek }.toString()

        val latestTimestamp = sessions.first().timestamp
        val relativeDate = formatRelativeDate(app, latestTimestamp)

        _uiState.update {
            it.copy(
                totalCleans = total,
                thisWeekCleans = thisWeek,
                lastCleaned = relativeDate,
                isLoading = false
            )
        }
    }

    private fun formatRelativeDate(app: Application, timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diffMs = (now - timestamp).coerceAtLeast(0)
        val diffDays = TimeUnit.MILLISECONDS.toDays(diffMs).toInt()

        val calNow = Calendar.getInstance().apply { timeInMillis = now }
        val calThen = Calendar.getInstance().apply { timeInMillis = timestamp }

        val isSameDay = calNow.get(Calendar.YEAR) == calThen.get(Calendar.YEAR) &&
                calNow.get(Calendar.DAY_OF_YEAR) == calThen.get(Calendar.DAY_OF_YEAR)

        if (isSameDay) {
            return app.getString(R.string.stat_today)
        }

        calNow.add(Calendar.DAY_OF_YEAR, -1)
        val isYesterday = calNow.get(Calendar.YEAR) == calThen.get(Calendar.YEAR) &&
                calNow.get(Calendar.DAY_OF_YEAR) == calThen.get(Calendar.DAY_OF_YEAR)

        if (isYesterday) {
            return app.getString(R.string.stat_yesterday)
        }

        return if (diffDays in 2..30) {
            app.getString(R.string.stat_days_ago, diffDays)
        } else {
            app.getString(R.string.stat_days_ago, diffDays.coerceAtLeast(1))
        }
    }

    /**
     * Toggles Quick Clean mode.
     * Sweeps continuously from 10 Hz up to 1000 Hz over the user-configured duration
     * (default 15 seconds, adjustable in Settings).
     */
    fun onQuickCleanTapped() {
        if (_uiState.value.isQuickCleaning) {
            stopQuickClean()
            return
        }

        // SafetyCheck: Headphone check FIRST
        if (outputRouter.isHeadphonesConnected()) {
            _uiState.update {
                it.copy(
                    headphoneWarning = getApplication<Application>().getString(R.string.safety_headphones)
                )
            }
            return
        }

        _uiState.update { it.copy(headphoneWarning = null) }
        outputRouter.routeToSpeaker()

        val durationSec = settingsRepository.settings.value.quickCleanDurationSec
        val startFreq = 10f
        val endFreq = 1000f

        val started = audioPlayer.play(startFreq)
        if (!started) {
            _uiState.update {
                it.copy(errorMessage = "Failed to start audio playback.")
            }
            return
        }

        _uiState.update {
            it.copy(
                isQuickCleaning = true,
                quickCleanTotalDurationSec = durationSec,
                quickCleanRemainingSec = durationSec,
                currentQuickCleanFreq = 10,
                errorMessage = null
            )
        }

        quickCleanJob?.cancel()
        quickCleanJob = viewModelScope.launch {
            val totalSteps = durationSec * 10 // update every 100ms for smooth sweep
            for (step in 1..totalSteps) {
                delay(100L)
                if (outputRouter.isHeadphonesConnected()) {
                    stopQuickClean()
                    _uiState.update {
                        it.copy(
                            headphoneWarning = getApplication<Application>().getString(R.string.safety_stopped_headphones)
                        )
                    }
                    return@launch
                }

                val fraction = step.toFloat() / totalSteps.toFloat()
                val currentFreq = (startFreq + fraction * (endFreq - startFreq)).coerceIn(startFreq, endFreq)
                audioPlayer.updateFrequency(currentFreq)

                val remainingSec = ((totalSteps - step) / 10f).roundToInt().coerceAtLeast(0)
                _uiState.update {
                    it.copy(
                        quickCleanRemainingSec = remainingSec,
                        currentQuickCleanFreq = currentFreq.roundToInt()
                    )
                }
            }

            // Completed sweep
            audioPlayer.stop()
            val session = CleanSession(
                mode = "quick",
                durationSec = durationSec,
                peakFreqHz = 1000,
                output = "speaker"
            )
            historyRepository.addSession(session)

            _uiState.update {
                it.copy(
                    isQuickCleaning = false,
                    quickCleanRemainingSec = durationSec,
                    currentQuickCleanFreq = 10
                )
            }
        }
    }

    /**
     * Immediately terminates audio playback and coroutine for Quick Clean.
     * Called whenever navigating away or user pauses.
     */
    fun stopQuickClean() {
        quickCleanJob?.cancel()
        quickCleanJob = null
        audioPlayer.stop()
        val duration = settingsRepository.settings.value.quickCleanDurationSec
        _uiState.update {
            it.copy(
                isQuickCleaning = false,
                quickCleanRemainingSec = duration,
                currentQuickCleanFreq = 10
            )
        }
    }

    fun dismissHeadphoneWarning() {
        _uiState.update { it.copy(headphoneWarning = null) }
    }

    override fun onCleared() {
        super.onCleared()
        stopQuickClean()
        audioPlayer.release()
    }
}
