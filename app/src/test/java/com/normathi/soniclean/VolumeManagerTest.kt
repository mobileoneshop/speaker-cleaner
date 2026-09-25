package com.normathi.soniclean

import android.content.Context
import android.media.AudioManager
import androidx.test.core.app.ApplicationProvider
import com.normathi.soniclean.engine.audio.VolumeManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class VolumeManagerTest {

    @Test
    fun max_volume_is_applied_and_previous_volume_is_restored() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        // Set an arbitrary initial volume (e.g., 30% of max)
        val initialVolume = (maxVolume * 0.3f).toInt().coerceAtLeast(1)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, initialVolume, 0)
        assertEquals(initialVolume, audioManager.getStreamVolume(AudioManager.STREAM_MUSIC))

        val volumeManager = VolumeManager(context)

        // Maximize volume for ejection
        val success = volumeManager.setMaxVolumeForEjection()
        assertTrue("Setting max volume should succeed", success)
        assertTrue(volumeManager.isMaximized)
        assertEquals(maxVolume, audioManager.getStreamVolume(AudioManager.STREAM_MUSIC))

        // Restore volume after ejection finishes
        volumeManager.restoreVolume()
        assertFalse(volumeManager.isMaximized)
        assertEquals(initialVolume, audioManager.getStreamVolume(AudioManager.STREAM_MUSIC))
    }
}
