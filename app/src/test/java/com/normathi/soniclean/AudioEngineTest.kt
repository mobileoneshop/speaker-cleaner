package com.normathi.soniclean

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.normathi.soniclean.engine.audio.AudioConfig
import com.normathi.soniclean.engine.audio.AudioEngine
import com.normathi.soniclean.engine.audio.AudioPlayerState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.abs

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AudioEngineTest {

    private lateinit var app: Application
    private lateinit var audioEngine: AudioEngine

    @Before
    fun setup() {
        app = ApplicationProvider.getApplicationContext()
        audioEngine = AudioEngine(app)
    }

    @Test
    fun calibratedWaterEjectionFrequency_is165Hz() {
        assertEquals(165.0f, AudioEngine.CALIBRATED_WATER_EJECT_FREQ, 0.001f)
        assertEquals(165.0f, AudioEngine.FREQ_PRIMARY_WATER_EJECT, 0.001f)
    }

    @Test
    fun highFrequencyPresets_withinSupportedRange() {
        assertEquals(12000.0f, AudioEngine.FREQ_HIGH_DUST_EJECTION, 0.001f)
        assertEquals(15000.0f, AudioEngine.FREQ_HIGH_ULTRASONIC_MIST, 0.001f)
        assertEquals(18000.0f, AudioEngine.FREQ_HIGH_RESONANCE_EXCITATION, 0.001f)
        assertEquals(20000.0f, AudioEngine.FREQ_HIGH_MAX_AUDIBLE, 0.001f)

        assertTrue(AudioEngine.FREQ_HIGH_DUST_EJECTION <= AudioEngine.MAX_SUPPORTED_FREQ_HZ)
        assertTrue(AudioEngine.FREQ_HIGH_ULTRASONIC_MIST <= AudioEngine.MAX_SUPPORTED_FREQ_HZ)
        assertTrue(AudioEngine.FREQ_HIGH_RESONANCE_EXCITATION <= AudioEngine.MAX_SUPPORTED_FREQ_HZ)
        assertTrue(AudioEngine.FREQ_HIGH_MAX_AUDIBLE <= AudioEngine.MAX_SUPPORTED_FREQ_HZ)
    }

    @Test
    fun sampleRateAndNyquistFrequency_areValid() {
        assertEquals(AudioConfig.SAMPLE_RATE, audioEngine.sampleRate)
        assertEquals(22050.0f, audioEngine.nyquistFrequency, 0.001f)
        assertTrue(audioEngine.effectiveMaxFrequency <= audioEngine.nyquistFrequency)
    }

    @Test
    fun calculateOptimalTrackBufferSize_returnsAlignedAndSufficientBytes() {
        val minBufferSize = 4096
        val optimal = AudioEngine.calculateOptimalTrackBufferSize(AudioConfig.SAMPLE_RATE, minBufferSize)
        // Must be even (16-bit frame aligned)
        assertEquals(0, optimal % 2)
        // Must be at least double minBufferSize
        assertTrue(optimal >= minBufferSize * 2)
        // Must be at least 8192 bytes
        assertTrue(optimal >= AudioEngine.DEFAULT_BUFFER_SIZE_SAMPLES * 4)
    }

    @Test
    fun generateCalibratedSineWave_producesCorrectBufferSize() {
        val duration = 1.0 // 1 second
        val sampleRate = AudioConfig.SAMPLE_RATE
        val buffer = AudioEngine.generateCalibratedSineWave(
            frequencyHz = AudioEngine.CALIBRATED_WATER_EJECT_FREQ,
            durationSeconds = duration,
            sampleRate = sampleRate
        )
        val expectedSamples = (sampleRate * duration).toInt()
        assertEquals(expectedSamples, buffer.size)
    }

    @Test
    fun generateCalibratedSineWave_samplesWithin16BitBounds() {
        val buffer = AudioEngine.generateCalibratedSineWave(
            frequencyHz = 165.0f,
            durationSeconds = 0.5,
            amplitude = 1.0f
        )
        for (sample in buffer) {
            assertTrue(sample >= Short.MIN_VALUE)
            assertTrue(sample <= Short.MAX_VALUE)
        }
    }

    @Test
    fun generateCalibratedSineWave_zeroCrossingsMatch165Hz() {
        val frequency = 165.0
        val duration = 1.0 // 1 second
        val buffer = AudioEngine.generateCalibratedSineWave(
            frequencyHz = frequency.toFloat(),
            durationSeconds = duration,
            amplitude = 1.0f
        )

        var zeroCrossings = 0
        for (i in 1 until buffer.size) {
            if ((buffer[i - 1] < 0 && buffer[i] >= 0) || (buffer[i - 1] >= 0 && buffer[i] < 0)) {
                zeroCrossings++
            }
        }

        val expectedCrossings = 2 * frequency * duration // ~330 crossings per second
        val diff = abs(zeroCrossings - expectedCrossings)
        assertTrue("Zero crossings ($zeroCrossings) should be within 6 of expected ($expectedCrossings)", diff <= 6)
    }

    @Test
    fun generateHighFrequencySineWave_zeroCrossingsMatchHighFreq() {
        val frequency = 5000.0 // 5 kHz
        val duration = 0.1 // 100 ms
        val buffer = AudioEngine.generateHighFrequencySineWave(
            frequencyHz = frequency.toFloat(),
            durationSeconds = duration,
            amplitude = 1.0f
        )

        var zeroCrossings = 0
        for (i in 1 until buffer.size) {
            if ((buffer[i - 1] < 0 && buffer[i] >= 0) || (buffer[i - 1] >= 0 && buffer[i] < 0)) {
                zeroCrossings++
            }
        }

        val expectedCrossings = 2 * frequency * duration // ~1000 crossings
        val diff = abs(zeroCrossings - expectedCrossings)
        // Tolerance within 2%
        val tolerance = expectedCrossings * 0.02
        assertTrue("Zero crossings ($zeroCrossings) should be within tolerance ($tolerance) of expected ($expectedCrossings)", diff <= tolerance)
    }

    @Test
    fun generateWaterEjectionBurst_returnsValid1SecondBurst() {
        val burst = AudioEngine.generateWaterEjectionBurst(1.0)
        assertEquals(AudioConfig.SAMPLE_RATE, burst.size)
        // Anti-pop ramp means first sample is 0 or near 0
        assertEquals(0.toShort(), burst[0])
    }

    @Test
    fun audioEngine_initialStateIsIdle() {
        assertEquals(AudioPlayerState.Idle, audioEngine.state.value)
        assertFalse(audioEngine.isPlaying)
        assertEquals(AudioEngine.CALIBRATED_WATER_EJECT_FREQ, audioEngine.currentFrequency, 0.001f)
    }

    @Test
    fun audioEngine_updateFrequencyClampsToSupportedSpectrum() {
        // Normal 165Hz
        audioEngine.setFrequency(165.0f)
        assertEquals(165.0f, audioEngine.currentFrequency, 0.001f)

        // High frequency 1000Hz (sound test sweep)
        audioEngine.setFrequency(1000.0f)
        assertEquals(1000.0f, audioEngine.currentFrequency, 0.001f)

        // High frequency 15000Hz (ultrasonic mist)
        audioEngine.setFrequency(15000.0f)
        assertEquals(15000.0f, audioEngine.currentFrequency, 0.001f)

        // Below minimum supported bound (20Hz)
        audioEngine.setFrequency(5.0f)
        assertEquals(AudioEngine.MIN_SUPPORTED_FREQ_HZ, audioEngine.currentFrequency, 0.001f)

        // Above effective maximum bound
        audioEngine.setFrequency(30000.0f)
        assertEquals(audioEngine.effectiveMaxFrequency, audioEngine.currentFrequency, 0.001f)
    }

    @Test
    fun audioEngine_volumeClamping() {
        audioEngine.setVolume(1.5f)
        assertEquals(1.0f, audioEngine.currentVolume, 0.001f)

        audioEngine.setVolume(-0.5f)
        assertEquals(0.0f, audioEngine.currentVolume, 0.001f)

        audioEngine.setVolume(0.8f)
        assertEquals(0.8f, audioEngine.currentVolume, 0.001f)
    }

    @Test
    fun audioEngine_stopSafelyResetsState() {
        audioEngine.stop()
        assertFalse(audioEngine.isPlaying)
        assertEquals(AudioPlayerState.Idle, audioEngine.state.value)
    }

    @Test
    fun audioEngine_aliasResolvesCorrectly() {
        val engineAlias: com.normathi.soniclean.engine.AudioEngine = audioEngine
        assertNotNull(engineAlias)
        assertEquals(165.0f, com.normathi.soniclean.engine.AudioEngine.CALIBRATED_WATER_EJECT_FREQ, 0.001f)
    }
}
