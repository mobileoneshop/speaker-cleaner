package com.normathi.soniclean.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.normathi.soniclean.ui.theme.DarkAccent
import com.normathi.soniclean.ui.theme.DarkAccentSecondary
import kotlin.math.sin

/**
 * Real-time animated audio visualizer.
 * Renders 48 vertical bars with rounded caps whose motion correlates with the active
 * sound frequency. Smoothly settles to a baseline when playback halts.
 */
@Composable
fun WaveformVisualizer(
    isPlaying: Boolean,
    frequencyHz: Float,
    modifier: Modifier = Modifier,
    barCount: Int = 48
) {
    var animationTime by remember { mutableFloatStateOf(0f) }
    val settleFactor = remember { Animatable(0f) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            settleFactor.animateTo(1.0f, animationSpec = tween(300))
        } else {
            settleFactor.animateTo(0.0f, animationSpec = tween(400))
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                withFrameNanos { timeNanos ->
                    animationTime = (timeNanos / 1_000_000_000f)
                }
            }
        }
    }

    val gradientBrush = remember {
        Brush.verticalGradient(
            colors = listOf(DarkAccent, DarkAccentSecondary)
        )
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .testTag("waveform_visualizer")
    ) {
        val totalWidth = size.width
        val canvasHeight = size.height
        val barSpacing = totalWidth / barCount
        val barWidth = (barSpacing * 0.65f).coerceAtLeast(2f)
        val cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        val settle = settleFactor.value

        val baseFreq = frequencyHz.coerceIn(50f, 1000f)
        val speedFactor = baseFreq / 60f

        for (i in 0 until barCount) {
            val wave = sin(animationTime * speedFactor + i * 0.38f)
            val normalizedWave = 0.5f + 0.5f * wave
            // Baseline idle height is 4dp, animated height can reach up to 90% of canvas height
            val minHeight = 4.dp.toPx()
            val dynamicMax = canvasHeight * 0.88f
            val activeHeight = minHeight + (dynamicMax - minHeight) * normalizedWave
            val currentBarHeight = minHeight + (activeHeight - minHeight) * settle

            val x = i * barSpacing + (barSpacing - barWidth) / 2f
            val y = (canvasHeight - currentBarHeight) / 2f

            drawRoundRect(
                brush = gradientBrush,
                topLeft = Offset(x, y),
                size = Size(barWidth, currentBarHeight),
                cornerRadius = cornerRadius
            )
        }
    }
}
