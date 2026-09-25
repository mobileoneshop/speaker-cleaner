package com.normathi.soniclean.engine.audio

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build

enum class AudioOutputTarget {
    SPEAKER,
    EARPIECE
}

/**
 * Handles audio output routing between the main loudspeaker and earpiece speaker,
 * with state restoration and connected peripheral detection.
 */
class AudioOutputRouter(context: Context) {

    private val audioManager = context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private var previousMode: Int = AudioManager.MODE_NORMAL
    private var previousSpeakerphoneOn: Boolean = false
    private var isModified: Boolean = false

    var currentTarget: AudioOutputTarget = AudioOutputTarget.SPEAKER
        private set

    /**
     * Checks if headphones, earphones, or Bluetooth audio output devices are connected.
     * Safety non-negotiable rule: Cleaning must be blocked if headphones are detected.
     */
    fun isHeadphonesConnected(): Boolean {
        val am = audioManager ?: return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val devices = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            for (device in devices) {
                when (device.type) {
                    AudioDeviceInfo.TYPE_WIRED_HEADSET,
                    AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                    AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                    AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                    AudioDeviceInfo.TYPE_USB_HEADSET -> return true
                }
            }
            return false
        } else {
            @Suppress("DEPRECATION")
            return am.isWiredHeadsetOn || am.isBluetoothA2dpOn || am.isBluetoothScoOn
        }
    }

    /**
     * Route audio to loudspeaker (default for water/dust ejection).
     */
    fun routeToSpeaker() {
        val am = audioManager ?: return
        captureOriginalState()
        am.mode = AudioManager.MODE_NORMAL
        @Suppress("DEPRECATION")
        am.isSpeakerphoneOn = true
        currentTarget = AudioOutputTarget.SPEAKER
    }

    /**
     * Route audio to earpiece speaker.
     * Uses MODE_IN_COMMUNICATION to direct playback to the earpiece.
     */
    fun routeToEarpiece() {
        val am = audioManager ?: return
        captureOriginalState()
        am.mode = AudioManager.MODE_IN_COMMUNICATION
        @Suppress("DEPRECATION")
        am.isSpeakerphoneOn = false
        currentTarget = AudioOutputTarget.EARPIECE
    }

    /**
     * Restores previous audio routing and mode states.
     */
    fun restore() {
        val am = audioManager ?: return
        if (!isModified) return
        try {
            am.mode = previousMode
            @Suppress("DEPRECATION")
            am.isSpeakerphoneOn = previousSpeakerphoneOn
        } catch (_: Throwable) {
            // Ignore restoration errors
        } finally {
            isModified = false
            currentTarget = AudioOutputTarget.SPEAKER
        }
    }

    private fun captureOriginalState() {
        val am = audioManager ?: return
        if (!isModified) {
            previousMode = am.mode
            @Suppress("DEPRECATION")
            previousSpeakerphoneOn = am.isSpeakerphoneOn
            isModified = true
        }
    }
}
