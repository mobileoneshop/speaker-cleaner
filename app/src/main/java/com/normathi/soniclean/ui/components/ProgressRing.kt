package com.normathi.soniclean.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.normathi.soniclean.ui.theme.DarkAccent
import com.normathi.soniclean.ui.theme.DarkAccentSecondary
import kotlin.math.roundToInt

/**
 * Circular progress ring for water ejection sequence.
 * 200dp diameter with 14dp gradient sweep stroke and animated percentage count.
 */
@Composable
fun ProgressRing(
    progress: Float, // 0.0f to 1.0f
    currentFrequencyHz: Float,
    isCleaning: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 200),
        label = "ProgressRingAnimation"
    )

    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val strokeWidth = 14.dp
    val gradientBrush = Brush.sweepGradient(
        colors = listOf(DarkAccent, DarkAccentSecondary, DarkAccent)
    )

    Box(
        modifier = modifier
            .size(200.dp)
            .testTag("progress_ring"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(200.dp)) {
            val strokePx = strokeWidth.toPx()
            val diameter = size.minDimension - strokePx
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)
            val arcSize = Size(diameter, diameter)

            // Background track
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Animated progress arc
            val sweepAngle = animatedProgress * 360f
            if (sweepAngle > 0f) {
                drawArc(
                    brush = gradientBrush,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        // Center readout
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val percentage = (animatedProgress * 100f).roundToInt()
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.testTag("progress_percentage_text")
            )

            Spacer(modifier = Modifier.height(4.dp))

            val subtitleText = if (isCleaning) {
                "${currentFrequencyHz.toInt()} Hz"
            } else if (progress >= 1f) {
                "Cleaned"
            } else {
                "Ready"
            }

            Text(
                text = subtitleText,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (isCleaning) DarkAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("progress_frequency_text")
            )
        }
    }
}
