package com.normathi.soniclean

import com.normathi.soniclean.engine.audio.AudioConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioConfigTest {

    @Test
    fun audio_config_manual_range_and_default() {
        assertEquals(10, AudioConfig.MIN_FREQ)
        assertEquals(1000, AudioConfig.MAX_FREQ)
        assertEquals(180, AudioConfig.DEFAULT_MANUAL_FREQ)
        assertTrue(AudioConfig.DEFAULT_MANUAL_FREQ in AudioConfig.MIN_FREQ..AudioConfig.MAX_FREQ)
    }

    @Test
    fun auto_stop_limits_within_safety_guidelines() {
        assertTrue(
            "Max session duration must be <= 60 seconds",
            AudioConfig.MAX_SESSION_DURATION_SEC <= 60
        )
        assertTrue(
            "Earpiece burst duration must be <= 10 seconds",
            AudioConfig.EARPIECE_BURST_MAX_SEC <= 10
        )
        assertTrue(
            "Earpiece volume cap must be <= 50%",
            AudioConfig.EARPIECE_MAX_VOLUME_RATIO <= 0.5f
        )
    }
}
