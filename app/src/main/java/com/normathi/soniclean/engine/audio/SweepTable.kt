package com.normathi.soniclean.engine.audio

/**
 * Definition of a single step in a cleaning cycle.
 */
data class CleanCycleStep(
    val stepIndex: Int,
    val title: String,
    val startFreqHz: Float,
    val endFreqHz: Float,
    val durationSec: Float,
    val isSweep: Boolean = false,
    val isSilence: Boolean = false
) {
    /**
     * Calculates the frequency at a given elapsed time within this step.
     */
    fun frequencyAt(elapsedSec: Float): Float {
        if (isSilence) return 0f
        if (!isSweep || durationSec <= 0f) return startFreqHz
        val progress = (elapsedSec / durationSec).coerceIn(0f, 1f)
        return startFreqHz + (endFreqHz - startFreqHz) * progress
    }
}

/**
 * Pre-engineered frequency tables for water ejection and speaker cleaning.
 * Extends well beyond 200 Hz up to 1200 Hz for thorough multi-stage cleaning.
 */
object SweepTable {

    /**
     * The signature 4-step Auto Clean cycle with high-frequency coverage (165 Hz to 1200 Hz).
     * Total duration ≈ 33.5 seconds (displayed as 34s).
     */
    val AUTO_CYCLE: List<CleanCycleStep> = listOf(
        // Step 1: 165 Hz primary resonance (8s)
        CleanCycleStep(
            stepIndex = 1,
            title = "Step 1 of 4 · 165 Hz Resonance",
            startFreqHz = 165f,
            endFreqHz = 165f,
            durationSec = 8.0f
        ),
        // Gap 1: 0.5s silence
        CleanCycleStep(
            stepIndex = 1,
            title = "Pulse interval",
            startFreqHz = 0f,
            endFreqHz = 0f,
            durationSec = 0.5f,
            isSilence = true
        ),
        // Step 2: 300 Hz -> 600 Hz mid-range displacement (8s)
        CleanCycleStep(
            stepIndex = 2,
            title = "Step 2 of 4 · Sweep 300→600 Hz",
            startFreqHz = 300f,
            endFreqHz = 600f,
            durationSec = 8.0f,
            isSweep = true
        ),
        // Gap 2: 0.5s silence
        CleanCycleStep(
            stepIndex = 2,
            title = "Pulse interval",
            startFreqHz = 0f,
            endFreqHz = 0f,
            durationSec = 0.5f,
            isSilence = true
        ),
        // Step 3: 600 Hz -> 1000 Hz high acoustic ejection (8s)
        CleanCycleStep(
            stepIndex = 3,
            title = "Step 3 of 4 · Sweep 600→1000 Hz",
            startFreqHz = 600f,
            endFreqHz = 1000f,
            durationSec = 8.0f,
            isSweep = true
        ),
        // Gap 3: 0.5s silence
        CleanCycleStep(
            stepIndex = 3,
            title = "Pulse interval",
            startFreqHz = 0f,
            endFreqHz = 0f,
            durationSec = 0.5f,
            isSilence = true
        ),
        // Step 4: 1200 Hz -> 200 Hz full-spectrum flush (8s)
        CleanCycleStep(
            stepIndex = 4,
            title = "Step 4 of 4 · Sweep 1200→200 Hz",
            startFreqHz = 1200f,
            endFreqHz = 200f,
            durationSec = 8.0f,
            isSweep = true
        )
    )

    /**
     * Total duration of the Auto Clean cycle in seconds.
     */
    val TOTAL_AUTO_CYCLE_DURATION_SEC: Float = AUTO_CYCLE.sumOf { it.durationSec.toDouble() }.toFloat()

    /**
     * Sound test sweep: 200 Hz to 1000 Hz over 5 seconds.
     */
    val SOUND_TEST_STEP = CleanCycleStep(
        stepIndex = 1,
        title = "Sound Test Sweep",
        startFreqHz = AudioConfig.TEST_SWEEP_START_FREQ.toFloat(),
        endFreqHz = AudioConfig.TEST_SWEEP_END_FREQ.toFloat(),
        durationSec = AudioConfig.TEST_SWEEP_DURATION_SEC.toFloat(),
        isSweep = true
    )
}
