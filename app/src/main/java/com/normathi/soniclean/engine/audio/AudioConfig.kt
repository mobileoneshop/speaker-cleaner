package com.normathi.soniclean.engine.audio

/**
 * Audio parameters for the SonicClean sound synthesis engine.
 * Never hardcode frequencies, sample rates, or durations in UI code.
 */
object AudioConfig {
    const val SAMPLE_RATE = 44100
    const val CHANNELS = 1 // Mono
    const val BITS_PER_SAMPLE = 16
    const val BUFFER_SIZE_SAMPLES = 2048

    // Frequencies (Hz) for Manual and Quick Clean modes
    const val DEFAULT_MANUAL_FREQ = 180
    const val MIN_FREQ = 10
    const val MAX_FREQ = 1000

    // High-frequency acoustic resonance and full-spectrum range (Hz)
    const val MIN_AUDIO_FREQ = 20
    const val MAX_AUDIO_FREQ = 22000
    const val HIGH_FREQ_DUST_EJECT = 12000
    const val HIGH_FREQ_ULTRASONIC = 15000
    const val HIGH_FREQ_RESONANCE = 18000
    const val HIGH_FREQ_MAX_AUDIBLE = 20000

    // Sound Test Sweep (Hz)
    const val TEST_SWEEP_START_FREQ = 200
    const val TEST_SWEEP_END_FREQ = 1000
    const val TEST_SWEEP_DURATION_SEC = 5

    // Durations (seconds)
    const val MAX_SESSION_DURATION_SEC = 60
    const val EARPIECE_BURST_MAX_SEC = 10
    const val EARPIECE_MAX_VOLUME_RATIO = 0.5f

    // Ramp duration in milliseconds to prevent audio pops
    const val RAMP_DURATION_MS = 25
}
