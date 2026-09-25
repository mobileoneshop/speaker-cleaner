package com.normathi.soniclean

import com.normathi.soniclean.engine.audio.SweepTable
import org.junit.Assert.assertTrue
import org.junit.Test

class SweepTableTest {

    @Test
    fun auto_cycle_sums_to_33_to_35_seconds() {
        val totalDuration = SweepTable.TOTAL_AUTO_CYCLE_DURATION_SEC
        assertTrue(
            "Auto cycle duration ($totalDuration) must be between 33 and 35 seconds",
            totalDuration in 33.0f..35.0f
        )
    }

    @Test
    fun auto_cycle_frequencies_within_50_to_1500_hz() {
        var foundAbove500Hz = false
        for (step in SweepTable.AUTO_CYCLE) {
            if (step.isSilence) continue
            assertTrue(
                "Start freq ${step.startFreqHz} must be between 50 and 1500 Hz",
                step.startFreqHz in 50.0f..1500.0f
            )
            assertTrue(
                "End freq ${step.endFreqHz} must be between 50 and 1500 Hz",
                step.endFreqHz in 50.0f..1500.0f
            )
            if (step.startFreqHz > 500f || step.endFreqHz > 500f) {
                foundAbove500Hz = true
            }
        }
        assertTrue("Auto clean cycle should reach above 500 Hz", foundAbove500Hz)
    }
}
