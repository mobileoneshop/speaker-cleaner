package com.normathi.soniclean.data.model

import java.util.UUID

/**
 * Data model for a completed cleaning session per brain/DATA_MODEL.md.
 */
data class CleanSession(
    val id: String = UUID.randomUUID().toString(),
    val mode: String, // "auto" | "manual" | "vibration" | "sound_test" | "quick"
    val durationSec: Int,
    val peakFreqHz: Int?, // null for vibration-only
    val output: String, // "speaker" | "earpiece"
    val timestamp: Long = System.currentTimeMillis()
)
