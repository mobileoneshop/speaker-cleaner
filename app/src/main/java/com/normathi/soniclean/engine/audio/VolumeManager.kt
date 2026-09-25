package com.normathi.soniclean.engine.audio

import android.content.Context
import android.media.AudioManager
import android.util.Log

/**
 * Manages audio stream volume during the water ejection process.
 * Ensures the device media volume is set to maximum strictly during ejection
 * and restored to the user's previous level afterward to prevent unexpected loud output.
 */
class VolumeManager(context: Context) {

    private val tag = "VolumeManager"
    private val audioManager = context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private var originalVolume: Int? = null
    private var isVolumeMaximized = false

    /**
     * Captures the current media volume and sets volume to maximum for ejection.
     * @return true if volume was successfully set to maximum.
     */
    @Synchronized
    fun setMaxVolumeForEjection(): Boolean {
        val am = audioManager ?: return false
        try {
            val current = am.getStreamVolume(AudioManager.STREAM_MUSIC)
            val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

            if (!isVolumeMaximized) {
                originalVolume = current
                isVolumeMaximized = true
                Log.d(tag, "Saved original volume: $current (max is $max)")
            }

            am.setStreamVolume(AudioManager.STREAM_MUSIC, max, 0)
            Log.d(tag, "Set media volume to maximum ($max) for ejection")
            return true
        } catch (e: Throwable) {
            Log.e(tag, "Failed to set volume to max for ejection", e)
            return false
        }
    }

    /**
     * Restores media volume to the level it was before ejection began.
     */
    @Synchronized
    fun restoreVolume() {
        val am = audioManager ?: return
        if (!isVolumeMaximized) return

        val previous = originalVolume
        if (previous != null) {
            try {
                am.setStreamVolume(AudioManager.STREAM_MUSIC, previous, 0)
                Log.d(tag, "Restored media volume to previous level: $previous")
            } catch (e: Throwable) {
                Log.e(tag, "Failed to restore previous volume level", e)
            } finally {
                originalVolume = null
                isVolumeMaximized = false
            }
        } else {
            isVolumeMaximized = false
        }
    }

    /**
     * Checks if volume is currently boosted to max by the ejection process.
     */
    val isMaximized: Boolean
        get() = isVolumeMaximized
}
