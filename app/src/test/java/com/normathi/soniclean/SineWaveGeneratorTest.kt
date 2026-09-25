package com.normathi.soniclean

import com.normathi.soniclean.engine.audio.AudioConfig
import com.normathi.soniclean.engine.audio.ContinuousSineGenerator
import com.normathi.soniclean.engine.audio.SineWaveGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class SineWaveGeneratorTest {

    @Test
    fun buffer_length_equals_sampleRate_times_seconds() {
        val sampleRate = AudioConfig.SAMPLE_RATE
        val duration = 1.5 // 1.5 seconds
        val buffer = SineWaveGenerator.generatePcm16Buffer(
            frequencyHz = 165.0,
            durationSeconds = duration,
            sampleRate = sampleRate
        )
        val expectedLength = (sampleRate * duration).toInt()
        assertEquals(expectedLength, buffer.size)
    }

    @Test
    fun normalized_amplitude_within_bounds() {
        val samples = SineWaveGenerator.generateNormalizedSamples(
            frequencyHz = 200.0,
            durationSeconds = 1.0,
            amplitude = 1.0f
        )
        for (sample in samples) {
            assertTrue("Sample $sample must be >= -1.0", sample >= -1.0f)
            assertTrue("Sample $sample must be <= 1.0", sample <= 1.0f)
        }
    }

    @Test
    fun zero_crossings_match_frequency_within_tolerance() {
        val frequency = 200.0
        val duration = 1.0 // 1 second
        val samples = SineWaveGenerator.generateNormalizedSamples(
            frequencyHz = frequency,
            durationSeconds = duration,
            amplitude = 1.0f
        )

        // Count zero crossings
        var zeroCrossings = 0
        for (i in 1 until samples.size) {
            if ((samples[i - 1] < 0 && samples[i] >= 0) || (samples[i - 1] >= 0 && samples[i] < 0)) {
                zeroCrossings++
            }
        }

        val expectedCrossings = 2 * frequency * duration
        val delta = abs(zeroCrossings - expectedCrossings)
        val percentageDiff = (delta / expectedCrossings) * 100.0

        assertTrue(
            "Zero crossings count ($zeroCrossings) must be within 2% of expected ($expectedCrossings)",
            percentageDiff <= 2.0
        )
    }

    @Test
    fun continuous_generator_produces_non_empty_buffers() {
        val generator = ContinuousSineGenerator()
        generator.frequencyHz = 165f
        val buffer = ShortArray(1024)
        val written = generator.fillBuffer(buffer)

        assertEquals(1024, written)

        var hasNonZero = false
        for (sample in buffer) {
            if (sample != 0.toShort()) {
                hasNonZero = true
                break
            }
        }
        assertTrue("Generated buffer should contain sound samples", hasNonZero)
    }
}
