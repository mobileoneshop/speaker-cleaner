package com.normathi.soniclean.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.normathi.soniclean.data.model.CleanSession
import com.normathi.soniclean.data.repository.DefaultHistoryRepository
import com.normathi.soniclean.data.repository.HistoryRepository
import kotlinx.coroutines.flow.StateFlow

class HistoryViewModel @JvmOverloads constructor(
    application: Application,
    private val historyRepository: HistoryRepository = DefaultHistoryRepository.getInstance(application)
) : AndroidViewModel(application) {

    val sessions: StateFlow<List<CleanSession>> = historyRepository.sessions

    fun clearAllHistory() {
        historyRepository.clearAll()
    }
}
