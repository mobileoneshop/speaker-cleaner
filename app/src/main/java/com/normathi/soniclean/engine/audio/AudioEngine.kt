package com.normathi.soniclean.engine.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

/**
 * High-performance, calibrated Audio Engine using Android's AudioTrack API.
 * Synthesizes pure 16-bit PCM mono sine waves across a wide acoustic spectrum
 * (from low resonant frequencies like 165 Hz for water ejection up to high-frequency
 * acoustic waves like 12 kHz - 20 kHz for speaker resonance and dust clearance).
 *
 * Implements optimal sample rate and buffer sizing to ensure continuous acoustic pressure,
 * prevent buffer underruns, and eliminate harmonic distortion or audible pops.
 */
open class AudioEngine(
    private val context: Context? = null,
    val sampleRate: Int = resolveSampleRate(context)
) : AudioPlayer {

    companion object {
        private const val TAG = "SonicCleanAudioEngine"

        /**
         * Calibrated acoustic resonance frequency for smartphone speaker water ejection.
         * 165 Hz maximizes speaker diaphragm excursion to create high-velocity
         * acoustic pressure waves that overcome the surface tension of water droplets
         * clinging to speaker mesh cavities.
         */
        const val CALIBRATED_WATER_EJECT_FREQ: Float = 165.0f

        // Cleaning frequency presets (Low / Mid acoustic resonance)
        const val FREQ_PRIMARY_WATER_EJECT: Float = 165.0f
        const val FREQ_LOW_RESONANCE: Float = 130.0f
        const val FREQ_WATER_EJECT_DETACH: Float = 200.0f
        const val FREQ_MID_EXPULSION: Float = 250.0f
        const val FREQ_FINE_MIST: Float = 300.0f
        const val FREQ_CLEAR_DISPERSAL: Float = 440.0f

        // High-frequency presets for acoustic resonance and dust/micro-debris clearance
        const val FREQ_HIGH_DUST_EJECTION: Float = 12000.0f
        const val FREQ_HIGH_ULTRASONIC_MIST: Float = 15000.0f
        const val FREQ_HIGH_RESONANCE_EXCITATION: Float = 18000.0f
        const val FREQ_HIGH_MAX_AUDIBLE: Float = 20000.0f

        // Absolute supported frequency bounds (Hz)
        const val MIN_SUPPORTED_FREQ_HZ: Float = 10.0f
        const val MAX_SUPPORTED_FREQ_HZ: Float = 22000.0f

        // Buffer sizing and envelope constants
        const val DEFAULT_BUFFER_SIZE_SAMPLES: Int = 2048
        const val RAMP_DURATION_MS: Int = 25
        const val MAX_SESSION_DURATION_MS: Long = 60_000L

        /**
         * Resolves the optimal sample rate for the device hardware, defaulting to 44.1 kHz.
         */
        fun resolveSampleRate(context: Context?): Int {
            if (context == null) return AudioConfig.SAMPLE_RATE
            val am = context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            val propRate = am?.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)?.toIntOrNull()
            return if (propRate != null && propRate in 8000..96000) {
                propRate
            } else {
                AudioConfig.SAMPLE_RATE
            }
        }

        /**
         * Calculates the optimal AudioTrack internal buffer size in bytes for continuous speaker resonance.
         * Ensures the buffer is large enough to prevent underruns under system scheduling jitter,
         * but small enough to maintain responsive frequency modulation.
         */
        fun calculateOptimalTrackBufferSize(sampleRate: Int, minBufferSizeInBytes: Int): Int {
            // Sizing: at least 2x minBufferSize and at least 2 buffers of 2048 16-bit mono samples (8192 bytes)
            val doubleMin = minBufferSizeInBytes * 2
            val minStreamingBytes = DEFAULT_BUFFER_SIZE_SAMPLES * 2 * 2 // 2 samples * 2 bytes/sample * 2 buffers
            val chosen = maxOf(doubleMin, minStreamingBytes)
            // Ensure 16-bit PCM frame alignment (even byte count)
            return if (chosen % 2 != 0) chosen + 1 else chosen
        }

        /**
         * Pure mathematical sine wave generator producing 16-bit PCM mono samples.
         * Handles both low-frequency and high-frequency wave synthesis with double precision.
         */
        fun generateCalibratedSineWave(
            frequencyHz: Float = CALIBRATED_WATER_EJECT_FREQ,
            durationSeconds: Double,
            sampleRate: Int = AudioConfig.SAMPLE_RATE,
            amplitude: Float = 1.0f
        ): ShortArray {
            val totalSamples = (sampleRate * durationSeconds).toInt()
            val buffer = ShortArray(totalSamples)
            val clampedAmp = amplitude.coerceIn(0.0f, 1.0f)
            val twoPi = 2.0 * PI
            val phaseIncrement = twoPi * frequencyHz.toDouble() / sampleRate
            var phase = 0.0

            val rampSamples = min(
                (sampleRate * (RAMP_DURATION_MS / 1000.0)).toInt(),
                totalSamples / 4
            )

            for (i in 0 until totalSamples) {
                // Apply soft anti-pop ramp in and ramp out
                val envelope = when {
                    i < rampSamples && rampSamples > 0 -> i.toFloat() / rampSamples
                    i > totalSamples - rampSamples && rampSamples > 0 -> (totalSamples - i).toFloat() / rampSamples
                    else -> 1.0f
                }
                val sampleVal = (sin(phase) * clampedAmp * envelope * Short.MAX_VALUE).toInt()
                buffer[i] = sampleVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                phase += phaseIncrement
                if (phase >= twoPi) phase %= twoPi
            }
            return buffer
        }

        /**
         * Generates high-frequency sine waves specifically tuned for high-velocity particle clearance.
         */
        fun generateHighFrequencySineWave(
            frequencyHz: Float = FREQ_HIGH_DUST_EJECTION,
            durationSeconds: Double = 1.0,
            sampleRate: Int = AudioConfig.SAMPLE_RATE,
            amplitude: Float = 1.0f
        ): ShortArray {
            return generateCalibratedSineWave(
                frequencyHz = frequencyHz,
                durationSeconds = durationSeconds,
                sampleRate = sampleRate,
                amplitude = amplitude
            )
        }

        /**
         * Convenience helper to generate a 1-second calibrated 165 Hz water ejection burst.
         */
        fun generateWaterEjectionBurst(durationSeconds: Double = 1.0): ShortArray {
            return generateCalibratedSineWave(
                frequencyHz = CALIBRATED_WATER_EJECT_FREQ,
                durationSeconds = durationSeconds,
                amplitude = 1.0f
            )
        }
    }

    /**
     * Theoretical maximum frequency that can be represented without aliasing (Nyquist limit).
     */
    val nyquistFrequency: Float
        get() = (sampleRate / 2.0f)

    /**
     * Effective maximum reproducible frequency with guard margin.
     */
    val effectiveMaxFrequency: Float
        get() = minOf(MAX_SUPPORTED_FREQ_HZ, nyquistFrequency - 50.0f)

    private val _state = MutableStateFlow<AudioPlayerState>(AudioPlayerState.Idle)
    override val state: StateFlow<AudioPlayerState> = _state.asStateFlow()

    private val isStreaming = AtomicBoolean(false)
    private var streamingThread: Thread? = null
    private var audioTrack: AudioTrack? = null

    @Volatile
    private var targetFrequencyHz: Float = CALIBRATED_WATER_EJECT_FREQ

    @Volatile
    private var targetVolume: Float = 1.0f

    @Volatile
    private var isPulseModeEnabled: Boolean = false

    @Volatile
    private var pulsePeriodMs: Long = 500L

    @Volatile
    private var pulseDutyCycle: Float = 0.75f

    private var playbackStartTimeMs: Long = 0L

    override val isPlaying: Boolean
        get() = isStreaming.get()

    override val currentFrequency: Float
        get() = targetFrequencyHz

    val currentVolume: Float
        get() = targetVolume

    /**
     * Start playing the calibrated sine wave frequency.
     * Defaults to the calibrated 165 Hz water ejection frequency.
     */
    @Synchronized
    fun start(
        frequencyHz: Float = CALIBRATED_WATER_EJECT_FREQ,
        volume: Float = 1.0f
    ): Boolean {
        setVolume(volume)
        return play(frequencyHz)
    }

    /**
     * Plays the specified frequency using AudioTrack.
     * Binds frequency generation directly for speaker water ejection and resonance.
     */
    override fun playFrequency(frequencyHz: Float): Boolean {
        return play(frequencyHz)
    }

    /**
     * Play the standard calibrated water ejection frequency (165 Hz).
     */
    fun playWaterEjection(): Boolean {
        return play(CALIBRATED_WATER_EJECT_FREQ)
    }

    /**
     * Start high-frequency acoustic resonance playback (e.g. 12 kHz dust clearance).
     */
    fun playHighFrequency(frequencyHz: Float = FREQ_HIGH_DUST_EJECTION): Boolean {
        return play(frequencyHz)
    }

    /**
     * Start acoustic impulse pulse mode (intermittent bursts) to dislodge stubborn moisture or debris.
     */
    @Synchronized
    fun startPulsedEjection(
        frequencyHz: Float = CALIBRATED_WATER_EJECT_FREQ,
        pulsePeriodMs: Long = 500L,
        dutyCycle: Float = 0.75f
    ): Boolean {
        this.isPulseModeEnabled = true
        this.pulsePeriodMs = pulsePeriodMs
        this.pulseDutyCycle = dutyCycle.coerceIn(0.1f, 0.95f)
        return play(frequencyHz)
    }

    /**
     * Implementation of [AudioPlayer.play].
     * Configures the Android AudioTrack API with proper sample rate and buffer sizing
     * for stable acoustic speaker resonance.
     */
    @Synchronized
    override fun play(frequencyHz: Float): Boolean {
        stop()

        targetFrequencyHz = frequencyHz.coerceIn(MIN_SUPPORTED_FREQ_HZ, effectiveMaxFrequency)

        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        if (minBufferSize <= 0) {
            val errorMsg = "Audio hardware unsupported or unavailable (buffer size: $minBufferSize)"
            Log.e(TAG, errorMsg)
            _state.value = AudioPlayerState.Error(errorMsg)
            return false
        }

        val optimalBufferSize = calculateOptimalTrackBufferSize(sampleRate, minBufferSize)

        try {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            val format = AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .build()

            audioTrack = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val builder = AudioTrack.Builder()
                    .setAudioAttributes(attributes)
                    .setAudioFormat(format)
                    .setBufferSizeInBytes(optimalBufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    builder.setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                }

                builder.build()
            } else {
                @Suppress("DEPRECATION")
                AudioTrack(
                    attributes,
                    format,
                    optimalBufferSize,
                    AudioTrack.MODE_STREAM,
                    AudioManager.AUDIO_SESSION_ID_GENERATE
                )
            }

            if (audioTrack?.state != AudioTrack.STATE_INITIALIZED) {
                audioTrack?.release()
                audioTrack = null
                val errorMsg = "Couldn't initialize speaker track for resonance synthesis."
                Log.e(TAG, errorMsg)
                _state.value = AudioPlayerState.Error(errorMsg)
                return false
            }

            audioTrack?.setVolume(targetVolume)
            audioTrack?.play()
        } catch (e: Throwable) {
            audioTrack?.release()
            audioTrack = null
            val errorMsg = "Couldn't access the speaker: ${e.localizedMessage ?: "Unknown error"}"
            Log.e(TAG, errorMsg, e)
            _state.value = AudioPlayerState.Error(errorMsg)
            return false
        }

        isStreaming.set(true)
        playbackStartTimeMs = SystemClock.elapsedRealtime()
        _state.value = AudioPlayerState.Playing(targetFrequencyHz)

        // Dedicated high-priority thread for continuous sine wave generation
        streamingThread = Thread({
            renderAudioLoop()
        }, "SonicCleanAudioEngineThread").apply {
            priority = Thread.MAX_PRIORITY
            start()
        }

        return true
    }

    /**
     * Real-time sine wave synthesis loop.
     * Uses double-precision phase accumulation with seamless wrapping and phase continuity
     * to eliminate harmonic distortion, clicks, or pops at both low and high frequencies.
     */
    private fun renderAudioLoop() {
        val buffer = ShortArray(DEFAULT_BUFFER_SIZE_SAMPLES)
        val twoPi = 2.0 * PI
        var phase = 0.0

        val rampSamples = (sampleRate * (RAMP_DURATION_MS / 1000.0)).toInt()
        var totalSamplesWritten = 0L

        try {
            while (isStreaming.get()) {
                // Safety cutoff: auto-stop after maximum session duration (60s)
                val elapsed = SystemClock.elapsedRealtime() - playbackStartTimeMs
                if (elapsed >= MAX_SESSION_DURATION_MS) {
                    Log.i(TAG, "AudioEngine reached maximum safety duration ($MAX_SESSION_DURATION_MS ms)")
                    break
                }

                val currentFreq = targetFrequencyHz.toDouble()
                val currentVol = targetVolume.coerceIn(0.0f, 1.0f)
                val phaseIncrement = twoPi * currentFreq / sampleRate

                val pulseActive = if (isPulseModeEnabled) {
                    val cyclePos = elapsed % pulsePeriodMs
                    val onDuration = (pulsePeriodMs * pulseDutyCycle).toLong()
                    cyclePos < onDuration
                } else {
                    true
                }

                val pulseMultiplier = if (pulseActive) 1.0f else 0.0f

                for (i in buffer.indices) {
                    // Soft attack envelope on playback start to prevent speaker pop
                    val attackEnvelope = if (totalSamplesWritten < rampSamples && rampSamples > 0) {
                        (totalSamplesWritten.toFloat() / rampSamples).coerceIn(0.0f, 1.0f)
                    } else {
                        1.0f
                    }

                    val effectiveAmp = currentVol * attackEnvelope * pulseMultiplier
                    val sampleVal = (sin(phase) * effectiveAmp * Short.MAX_VALUE).toInt()
                    buffer[i] = sampleVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()

                    phase += phaseIncrement
                    if (phase >= twoPi) {
                        phase %= twoPi
                    }
                    totalSamplesWritten++
                }

                val track = audioTrack ?: break
                if (track.playState != AudioTrack.PLAYSTATE_PLAYING) {
                    break
                }

                val written = track.write(buffer, 0, buffer.size, AudioTrack.WRITE_BLOCKING)
                if (written < 0) {
                    Log.w(TAG, "AudioTrack write returned status: $written")
                    break
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Audio rendering loop exception", e)
        } finally {
            stopInternal()
        }
    }

    /**
     * Dynamically update the sine wave frequency in real time.
     * Retains the running phase accumulator to ensure click-free acoustic resonance transitions.
     */
    override fun updateFrequency(frequencyHz: Float) {
        val clamped = frequencyHz.coerceIn(MIN_SUPPORTED_FREQ_HZ, effectiveMaxFrequency)
        targetFrequencyHz = clamped
        if (isStreaming.get()) {
            _state.value = AudioPlayerState.Playing(clamped)
        }
    }

    /**
     * Alias for [updateFrequency].
     */
    fun setFrequency(frequencyHz: Float) {
        updateFrequency(frequencyHz)
    }

    /**
     * Set playback volume level (0.0 to 1.0).
     */
    override fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0.0f, 1.0f)
        targetVolume = clamped
        try {
            audioTrack?.setVolume(clamped)
        } catch (e: Throwable) {
            Log.w(TAG, "Failed setting AudioTrack volume", e)
        }
    }

    /**
     * Stops audio synthesis and releases the current AudioTrack instance safely.
     */
    @Synchronized
    override fun stop() {
        if (!isStreaming.get() && audioTrack == null) return
        isStreaming.set(false)
        stopInternal()
    }

    @Synchronized
    override fun release() {
        stop()
    }

    private fun stopInternal() {
        isStreaming.set(false)
        isPulseModeEnabled = false
        try {
            audioTrack?.apply {
                if (playState == AudioTrack.PLAYSTATE_PLAYING) {
                    stop()
                }
                release()
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Exception during AudioTrack release", e)
        } finally {
            audioTrack = null
            streamingThread = null
            _state.value = AudioPlayerState.Idle
        }
    }
}
