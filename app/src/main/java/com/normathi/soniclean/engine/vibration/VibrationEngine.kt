package com.normathi.soniclean.engine.vibration

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

enum class VibrationPattern(val id: String, val titleResId: Int) {
    GENTLE("gentle", com.normathi.soniclean.R.string.vib_gentle),
    DEEP("deep", com.normathi.soniclean.R.string.vib_deep),
    PULSE("pulse", com.normathi.soniclean.R.string.vib_pulse)
}

interface VibrationController {
    val isSupported: Boolean
    fun startPattern(pattern: VibrationPattern)
    fun stop()
}

class VibrationEngine(private val context: Context) : VibrationController {

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    override val isSupported: Boolean
        get() = vibrator?.hasVibrator() == true

    override fun startPattern(pattern: VibrationPattern) {
        val vib = vibrator ?: return
        if (!vib.hasVibrator()) return

        stop()

        val timings: LongArray
        val amplitudes: IntArray?

        when (pattern) {
            VibrationPattern.GENTLE -> {
                // 200ms on / 400ms off repeating
                timings = longArrayOf(0, 200, 400)
                amplitudes = intArrayOf(0, 150, 0)
            }
            VibrationPattern.DEEP -> {
                // 500ms on / 500ms off repeating
                timings = longArrayOf(0, 500, 500)
                amplitudes = intArrayOf(0, 255, 0)
            }
            VibrationPattern.PULSE -> {
                // [0, 100, 100, 100, 300, 200]
                timings = longArrayOf(0, 100, 100, 100, 300, 200)
                amplitudes = intArrayOf(0, 255, 0, 200, 0, 255)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = if (vib.hasAmplitudeControl()) {
                    VibrationEffect.createWaveform(timings, amplitudes, 0)
                } else {
                    VibrationEffect.createWaveform(timings, 0)
                }
                vib.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(timings, 0)
            }
        } catch (_: Exception) {
            // Handle devices with unusual vibrator drivers safely
        }
    }

    override fun stop() {
        try {
            vibrator?.cancel()
        } catch (_: Exception) {
            // Safe cancel
        }
    }
}
