package com.normathi.soniclean.engine.audio

import kotlin.math.PI
import kotlin.math.sin

/**
 * Pure Kotlin sine wave generator.
 * Zero android.* dependencies to enable fast, deterministic JVM unit tests.
 */
object SineWaveGenerator {

    /**
     * Generates a single buffer of 16-bit PCM mono samples at the given frequency and duration.
     */
    fun generatePcm16Buffer(
        frequencyHz: Double,
        durationSeconds: Double,
        sampleRate: Int = AudioConfig.SAMPLE_RATE,
        amplitude: Float = 1.0f
    ): ShortArray {
        val totalSamples = (sampleRate * durationSeconds).toInt()
        val buffer = ShortArray(totalSamples)
        val clampedAmp = amplitude.coerceIn(0.0f, 1.0f)
        val twoPi = 2.0 * PI
        val phaseIncrement = twoPi * frequencyHz / sampleRate
        var phase = 0.0

        for (i in 0 until totalSamples) {
            val sampleVal = (sin(phase) * clampedAmp * Short.MAX_VALUE).toInt()
            buffer[i] = sampleVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            phase += phaseIncrement
            if (phase >= twoPi) {
                phase -= twoPi
            }
        }
        return buffer
    }

    /**
     * Generates normalized float samples in the range [-1.0, 1.0] for analysis and testing.
     */
    fun generateNormalizedSamples(
        frequencyHz: Double,
        durationSeconds: Double,
        sampleRate: Int = AudioConfig.SAMPLE_RATE,
        amplitude: Float = 1.0f
    ): FloatArray {
        val totalSamples = (sampleRate * durationSeconds).toInt()
        val buffer = FloatArray(totalSamples)
        val clampedAmp = amplitude.coerceIn(0.0f, 1.0f)
        val twoPi = 2.0 * PI
        val phaseIncrement = twoPi * frequencyHz / sampleRate
        var phase = 0.0

        for (i in 0 until totalSamples) {
            buffer[i] = (sin(phase) * clampedAmp).toFloat()
            phase += phaseIncrement
            if (phase >= twoPi) {
                phase -= twoPi
            }
        }
        return buffer
    }
}

/**
 * Streaming sine wave generator with phase preservation.
 * When frequency changes dynamically, the phase advances smoothly without jumps,
 * preventing clicks or audible pops.
 */
class ContinuousSineGenerator(
    val sampleRate: Int = AudioConfig.SAMPLE_RATE
) {
    @Volatile
    var frequencyHz: Float = AudioConfig.DEFAULT_MANUAL_FREQ.toFloat()

    @Volatile
    var amplitude: Float = 1.0f

    private var currentPhase: Double = 0.0
    private val twoPi = 2.0 * PI

    /**
     * Fills the provided buffer with continuous sine wave 16-bit PCM samples.
     * @return the number of samples written.
     */
    fun fillBuffer(buffer: ShortArray): Int {
        val freq = frequencyHz.toDouble()
        val amp = amplitude.coerceIn(0.0f, 1.0f)
        val phaseIncrement = twoPi * freq / sampleRate

        for (i in buffer.indices) {
            val sampleVal = (sin(currentPhase) * amp * Short.MAX_VALUE).toInt()
            buffer[i] = sampleVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            currentPhase += phaseIncrement
            if (currentPhase >= twoPi) {
                currentPhase -= twoPi
            }
        }
        return buffer.size
    }

    /**
     * Resets the internal phase to 0.
     */
    fun reset() {
        currentPhase = 0.0
    }
}
