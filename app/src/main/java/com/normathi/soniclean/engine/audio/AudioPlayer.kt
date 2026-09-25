package com.normathi.soniclean.engine.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

sealed class AudioPlayerState {
    object Idle : AudioPlayerState()
    data class Playing(val frequencyHz: Float) : AudioPlayerState()
    data class Error(val message: String) : AudioPlayerState()
}

interface AudioPlayer {
    val state: StateFlow<AudioPlayerState>
    val isPlaying: Boolean
    val currentFrequency: Float

    fun play(frequencyHz: Float): Boolean
    fun playFrequency(frequencyHz: Float): Boolean = play(frequencyHz)
    fun updateFrequency(frequencyHz: Float)
    fun setVolume(volume: Float)
    fun stop()
    fun release()
}

/**
 * Android AudioTrack-based audio player for high-frequency water and dust ejection.
 * Powered by [AudioEngine] for calibrated 16-bit PCM mono sine wave synthesis at 44100 Hz.
 */
class AndroidAudioPlayer(
    context: Context? = null
) : AudioEngine(context)

