package com.normathi.soniclean.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.normathi.soniclean.data.model.CleanSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

interface HistoryRepository {
    val sessions: StateFlow<List<CleanSession>>
    fun addSession(session: CleanSession)
    fun clearAll()
    fun getTotalCleans(): Int
    fun getThisWeekCleans(): Int
    fun getLastCleanedTimestamp(): Long?
}

class DefaultHistoryRepository(context: Context) : HistoryRepository {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _sessions = MutableStateFlow<List<CleanSession>>(emptyList())
    override val sessions: StateFlow<List<CleanSession>> = _sessions.asStateFlow()

    init {
        loadSessions()
    }

    private fun loadSessions() {
        val jsonString = prefs.getString(KEY_SESSIONS, null) ?: "[]"
        val list = mutableListOf<CleanSession>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    CleanSession(
                        id = obj.optString("id"),
                        mode = obj.optString("mode"),
                        durationSec = obj.optInt("durationSec"),
                        peakFreqHz = if (obj.has("peakFreqHz") && !obj.isNull("peakFreqHz")) {
                            obj.getInt("peakFreqHz")
                        } else null,
                        output = obj.optString("output", "speaker"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {
            // In case of corrupt JSON, start clean
        }
        _sessions.value = list
    }

    @Synchronized
    override fun addSession(session: CleanSession) {
        val currentList = _sessions.value.toMutableList()
        currentList.add(0, session) // newest first
        if (currentList.size > MAX_ENTRIES) {
            currentList.removeAt(currentList.lastIndex) // drop oldest
        }
        saveSessions(currentList)
    }

    @Synchronized
    override fun clearAll() {
        saveSessions(emptyList())
    }

    private fun saveSessions(list: List<CleanSession>) {
        val jsonArray = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("mode", item.mode)
                put("durationSec", item.durationSec)
                put("peakFreqHz", item.peakFreqHz)
                put("output", item.output)
                put("timestamp", item.timestamp)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_SESSIONS, jsonArray.toString()).apply()
        _sessions.value = list
    }

    override fun getTotalCleans(): Int = _sessions.value.size

    override fun getThisWeekCleans(): Int {
        val startOfWeek = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        return _sessions.value.count { it.timestamp >= startOfWeek }
    }

    override fun getLastCleanedTimestamp(): Long? {
        return _sessions.value.firstOrNull()?.timestamp
    }

    companion object {
        private const val PREFS_NAME = "sonic_clean_history"
        private const val KEY_SESSIONS = "clean_sessions_json"
        private const val MAX_ENTRIES = 100

        @Volatile
        private var instance: DefaultHistoryRepository? = null

        fun getInstance(context: Context): DefaultHistoryRepository {
            return instance ?: synchronized(this) {
                instance ?: DefaultHistoryRepository(context).also { instance = it }
            }
        }
    }
}
