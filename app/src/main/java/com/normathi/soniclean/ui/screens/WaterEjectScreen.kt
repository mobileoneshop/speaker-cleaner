package com.normathi.soniclean.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.normathi.soniclean.R
import com.normathi.soniclean.engine.audio.AudioEngine
import com.normathi.soniclean.ui.components.AppTopBar
import com.normathi.soniclean.ui.components.SafetyBanner
import com.normathi.soniclean.ui.components.WaveformVisualizer
import com.normathi.soniclean.ui.theme.AccentGradient
import com.normathi.soniclean.ui.theme.DangerRed
import com.normathi.soniclean.ui.theme.DarkAccent
import com.normathi.soniclean.ui.theme.SuccessGreen
import com.normathi.soniclean.viewmodel.WaterEjectViewModel
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Water Ejection Screen with:
 * 1. Prominent central 'Eject Water' button (bound to AudioEngine.playFrequency).
 * 2. Frequency range slider (100Hz to 200Hz) with 165Hz calibrated target.
 * 3. Countdown timer with battery protection to limit emission duration and prevent battery drain.
 */
@Composable
fun WaterEjectScreen(
    modifier: Modifier = Modifier,
    viewModel: WaterEjectViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler {
        viewModel.stopEjection(recordHistory = true)
        onNavigateBack()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopEjection(recordHistory = true)
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.eject_water_title),
                onNavigateBack = {
                    viewModel.stopEjection(recordHistory = true)
                    onNavigateBack()
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
            .fillMaxSize()
            .testTag("water_eject_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Safety notification if headphones are detected
            if (uiState.headphoneWarning != null) {
                SafetyBanner(
                    message = uiState.headphoneWarning ?: "",
                    onDismiss = { viewModel.dismissHeadphoneWarning() }
                )
            }

            // Completion notification after countdown expires
            AnimatedVisibility(
                visible = uiState.isCompleteNotice,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkAccent.copy(alpha = 0.12f)
                    ),
                    border = BorderStroke(1.dp, DarkAccent.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                        .testTag("countdown_complete_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = DarkAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.countdown_completed),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Battery preserved and speaker membrane protected.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Guidance & Battery Saver hint card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (uiState.isPlaying) Icons.Default.BatterySaver else Icons.Default.Info,
                        contentDescription = null,
                        tint = DarkAccent,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (uiState.isPlaying) {
                            "Battery protection active · Auto-stopping in ${uiState.remainingSeconds}s"
                        } else {
                            stringResource(R.string.eject_water_status_idle)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Central Hero "Eject Water" Action Button with Countdown Overlay
            CentralEjectWaterButton(
                isPlaying = uiState.isPlaying,
                remainingSeconds = uiState.remainingSeconds,
                durationLimitSeconds = uiState.durationLimitSeconds,
                onClick = { viewModel.onEjectWaterClicked() },
                modifier = Modifier.testTag("eject_water_button")
            )

            // Countdown Timer Display & Duration Controller
            CountdownTimerCard(
                isPlaying = uiState.isPlaying,
                remainingSeconds = uiState.remainingSeconds,
                durationLimitSeconds = uiState.durationLimitSeconds,
                progress = uiState.countdownProgress,
                onDurationSelected = { viewModel.onDurationLimitChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
            )

            // Frequency Range Adjustment Slider (100Hz to 200Hz)
            FrequencyAdjustmentCard(
                frequencyHz = uiState.frequencyHz,
                minFreqHz = uiState.minFrequencyHz,
                maxFreqHz = uiState.maxFrequencyHz,
                onFrequencyChanged = { viewModel.onFrequencyChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
            )

            // Acoustic Waveform Visualizer
            WaveformVisualizer(
                frequencyHz = uiState.frequencyHz,
                isPlaying = uiState.isPlaying,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .height(84.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Direct Composable binding [AudioEngine.playFrequency] directly on button click,
 * with countdown timer duration limiter.
 */
@Composable
fun WaterEjectContent(
    audioEngine: AudioEngine,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {}
) {
    var frequencyHz by remember { mutableFloatStateOf(AudioEngine.CALIBRATED_WATER_EJECT_FREQ) }
    var isPlaying by remember { mutableStateOf(audioEngine.isPlaying) }
    var durationLimitSeconds by remember { mutableIntStateOf(30) }
    var remainingSeconds by remember { mutableIntStateOf(30) }

    LaunchedEffect(isPlaying, durationLimitSeconds) {
        if (isPlaying) {
            remainingSeconds = durationLimitSeconds
            while (isPlaying && remainingSeconds > 0) {
                delay(1000L)
                remainingSeconds--
            }
            if (remainingSeconds <= 0 && isPlaying) {
                audioEngine.stop()
                isPlaying = false
                remainingSeconds = durationLimitSeconds
            }
        } else {
            remainingSeconds = durationLimitSeconds
        }
    }

    BackHandler {
        audioEngine.stop()
        onNavigateBack()
    }

    DisposableEffect(audioEngine) {
        onDispose {
            audioEngine.stop()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.eject_water_title),
                onNavigateBack = {
                    audioEngine.stop()
                    onNavigateBack()
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
            .fillMaxSize()
            .testTag("water_eject_screen_direct")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Central "Eject Water" button bound directly to audioEngine.playFrequency
            CentralEjectWaterButton(
                isPlaying = isPlaying,
                remainingSeconds = remainingSeconds,
                durationLimitSeconds = durationLimitSeconds,
                onClick = {
                    if (isPlaying) {
                        audioEngine.stop()
                        isPlaying = false
                    } else {
                        val started = audioEngine.playFrequency(frequencyHz)
                        isPlaying = started
                    }
                },
                modifier = Modifier.testTag("eject_water_button")
            )

            // Countdown timer duration limiter
            val progress = (1.0f - (remainingSeconds.toFloat() / durationLimitSeconds)).coerceIn(0f, 1f)
            CountdownTimerCard(
                isPlaying = isPlaying,
                remainingSeconds = remainingSeconds,
                durationLimitSeconds = durationLimitSeconds,
                progress = progress,
                onDurationSelected = {
                    durationLimitSeconds = it
                    if (!isPlaying) remainingSeconds = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
            )

            // Frequency Range Adjustment Slider (50Hz to 1000Hz)
            FrequencyAdjustmentCard(
                frequencyHz = frequencyHz,
                minFreqHz = 50.0f,
                maxFreqHz = 1000.0f,
                onFrequencyChanged = { newFreq ->
                    frequencyHz = newFreq
                    if (isPlaying) {
                        audioEngine.updateFrequency(newFreq)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
            )

            // Waveform visualizer
            WaveformVisualizer(
                frequencyHz = frequencyHz,
                isPlaying = isPlaying,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .height(84.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Prominent Central "Eject Water" circular hero button with acoustic ripples
 * and elapsed/countdown progress indicator.
 */
@Composable
private fun CentralEjectWaterButton(
    isPlaying: Boolean,
    remainingSeconds: Int,
    durationLimitSeconds: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AcousticRipples")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isPlaying) 1.25f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isPlaying) 700 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "RipplePulse"
    )

    val rippleAlpha by infiniteTransition.animateFloat(
        initialValue = if (isPlaying) 0.5f else 0.2f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isPlaying) 900 else 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RippleAlpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(240.dp)
            .padding(12.dp)
    ) {
        // Outer animated acoustic ripple ring
        Box(
            modifier = Modifier
                .size(220.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    color = if (isPlaying) DangerRed.copy(alpha = rippleAlpha) else DarkAccent.copy(alpha = rippleAlpha),
                    shape = CircleShape
                )
        )

        // Middle animated acoustic ripple ring
        Box(
            modifier = Modifier
                .size(195.dp)
                .scale(if (isPlaying) (pulseScale * 0.95f) else 1.0f)
                .clip(CircleShape)
                .background(
                    if (isPlaying) DangerRed.copy(alpha = 0.12f) else DarkAccent.copy(alpha = 0.08f)
                )
        )

        // Countdown Circular Progress Ring (visible while playing)
        if (isPlaying && durationLimitSeconds > 0) {
            val progressFraction = 1f - (remainingSeconds.toFloat() / durationLimitSeconds.toFloat()).coerceIn(0f, 1f)
            CircularProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier.size(175.dp),
                color = DangerRed,
                trackColor = Color.White.copy(alpha = 0.2f),
                strokeWidth = 4.dp,
                strokeCap = StrokeCap.Round
            )
        }

        // Core central Eject Water Button
        Surface(
            shape = CircleShape,
            color = Color.Transparent,
            shadowElevation = 8.dp,
            modifier = Modifier
                .size(165.dp)
                .clip(CircleShape)
                .clickable(onClick = onClick)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isPlaying) {
                            Brush.verticalGradient(listOf(DangerRed, Color(0xFFC62828)))
                        } else {
                            AccentGradient
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isPlaying) {
                            stringResource(R.string.eject_water_stop_btn)
                        } else {
                            stringResource(R.string.eject_water_btn)
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    if (isPlaying) {
                        Text(
                            text = String.format(Locale.US, "%02d:%02d", remainingSeconds / 60, remainingSeconds % 60),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Card containing the Countdown Timer duration limiter and Battery Saver controller.
 */
@Composable
private fun CountdownTimerCard(
    isPlaying: Boolean,
    remainingSeconds: Int,
    durationLimitSeconds: Int,
    progress: Float,
    onDurationSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.testTag("countdown_timer_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = DarkAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.countdown_timer_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Battery Saver indicator badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DarkAccent.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.BatterySaver,
                            contentDescription = null,
                            tint = DarkAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Battery Saver",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = DarkAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Timer display & Progress indicator
            if (isPlaying) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Time Remaining:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format(Locale.US, "%02d:%02d", remainingSeconds / 60, remainingSeconds % 60),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = DangerRed,
                        modifier = Modifier.testTag("countdown_timer_text")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = DangerRed,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                )
            } else {
                Text(
                    text = stringResource(R.string.countdown_timer_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Duration Limit Selector Chips (e.g. 15s, 30s, 45s, 60s)
            Text(
                text = "Select Max Duration:",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val durations = listOf(15, 30, 45, 60)
                durations.forEach { durationSec ->
                    val isSelected = durationLimitSeconds == durationSec
                    FilterChip(
                        selected = isSelected,
                        onClick = { onDurationSelected(durationSec) },
                        label = {
                            Text(
                                text = "${durationSec}s",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DarkAccent.copy(alpha = 0.22f),
                            selectedLabelColor = DarkAccent
                        ),
                        modifier = Modifier.testTag("duration_chip_$durationSec")
                    )
                }
            }
        }
    }
}

/**
 * Card containing the frequency slider (100Hz to 200Hz), readout, and resonant presets.
 */
@Composable
private fun FrequencyAdjustmentCard(
    frequencyHz: Float,
    minFreqHz: Float,
    maxFreqHz: Float,
    onFrequencyChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.testTag("frequency_adjustment_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Frequency numerical readout
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${frequencyHz.roundToInt()}",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 48.sp
                    ),
                    color = DarkAccent,
                    modifier = Modifier.testTag("water_eject_freq_readout")
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Hz",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Calibrated indicator badge
            val freqInt = frequencyHz.roundToInt()
            val isCalibrated = (freqInt == 165)
            val is180Peak = (freqInt == 180)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when {
                    isCalibrated -> DarkAccent.copy(alpha = 0.15f)
                    is180Peak -> SuccessGreen.copy(alpha = 0.15f)
                    else -> Color.Transparent
                },
                border = when {
                    isCalibrated -> BorderStroke(1.dp, DarkAccent.copy(alpha = 0.4f))
                    is180Peak -> BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.4f))
                    else -> null
                },
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = when {
                        isCalibrated -> "★ 165 Hz · Calibrated Water Resonance Sweet Spot"
                        is180Peak -> "★ 180 Hz · Acoustic Pressure Peak"
                        else -> "Resonant Frequency Tuning"
                    },
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isCalibrated || is180Peak) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = when {
                        isCalibrated -> DarkAccent
                        is180Peak -> SuccessGreen
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // The frequency slider
            Slider(
                value = frequencyHz,
                onValueChange = onFrequencyChanged,
                valueRange = minFreqHz..maxFreqHz,
                steps = 0,
                colors = SliderDefaults.colors(
                    thumbColor = DarkAccent,
                    activeTrackColor = DarkAccent,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("frequency_slider")
            )

            // Range boundary labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${minFreqHz.toInt()} Hz",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "165 Hz / 180 Hz",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = DarkAccent
                )
                Text(
                    text = "${maxFreqHz.toInt()} Hz",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick preset chips extending up to 1000 Hz
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val presets = listOf(130, 165, 180, 250, 440, 1000)
                presets.forEach { preset ->
                    val selected = frequencyHz.roundToInt() == preset
                    FilterChip(
                        selected = selected,
                        onClick = { onFrequencyChanged(preset.toFloat()) },
                        label = {
                            Text(
                                text = when (preset) {
                                    165 -> "★ 165 Hz"
                                    180 -> "★ 180 Hz"
                                    else -> "$preset Hz"
                                },
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DarkAccent.copy(alpha = 0.22f),
                            selectedLabelColor = DarkAccent
                        ),
                        modifier = Modifier.testTag("water_preset_chip_$preset")
                    )
                }
            }
        }
    }
}
