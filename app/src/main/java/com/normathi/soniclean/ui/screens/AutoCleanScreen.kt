package com.normathi.soniclean.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.normathi.soniclean.R
import com.normathi.soniclean.engine.audio.AudioOutputTarget
import com.normathi.soniclean.ui.components.ProgressRing
import com.normathi.soniclean.ui.components.SafetyFirstDialog
import com.normathi.soniclean.ui.components.WaveformVisualizer
import com.normathi.soniclean.ui.theme.AccentGradient
import com.normathi.soniclean.ui.theme.DangerRed
import com.normathi.soniclean.ui.theme.DarkAccent
import com.normathi.soniclean.ui.theme.SuccessGreen
import com.normathi.soniclean.ui.theme.WarningYellow
import com.normathi.soniclean.viewmodel.AutoCleanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoCleanScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AutoCleanViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("auto_clean_screen"),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.mode_auto),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.showSafetyDialogManually() },
                        modifier = Modifier.testTag("safety_info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Safety Information",
                            tint = DarkAccent
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Volume management status banner
                VolumeStatusCard(
                    isVolumeMaximized = uiState.isVolumeMaximized,
                    isCleaning = uiState.isCleaning
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Safety Guidance Banner
                SafetyGuidanceCard(
                    onClick = { viewModel.showSafetyDialogManually() }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Central Visual Progress Ring
                ProgressRing(
                    progress = uiState.progress,
                    currentFrequencyHz = uiState.currentFrequencyHz,
                    isCleaning = uiState.isCleaning,
                    modifier = Modifier.testTag("ejection_progress_ring")
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Sequence Step Label
                Text(
                    text = uiState.currentStepTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("step_title_text")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Progress duration
                val elapsed = uiState.elapsedSeconds.toInt()
                val total = uiState.totalDurationSeconds.toInt()
                Text(
                    text = "$elapsed / $total sec",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Live Waveform Visualizer
                WaveformVisualizer(
                    isPlaying = uiState.isCleaning,
                    frequencyHz = uiState.currentFrequencyHz,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Output Target Toggle (Speaker / Earpiece)
                OutputTogglePill(
                    currentTarget = uiState.outputTarget,
                    isCleaning = uiState.isCleaning,
                    onToggle = { viewModel.toggleOutputTarget() }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Primary 'Clean' / 'Stop' Trigger Button
            CleanActionButton(
                isCleaning = uiState.isCleaning,
                onClick = {
                    viewModel.onCleanTapped()
                }
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    // Safety First Dialog
    if (uiState.showSafetyDialog) {
        SafetyFirstDialog(
            onConfirm = { dontShowAgain ->
                viewModel.onSafetyConfirmed(dontShowAgain)
            },
            onDismiss = {
                viewModel.onSafetyDismissed()
            }
        )
    }

    // Safety Alert Dialog for Headphones
    if (uiState.showHeadphoneWarning) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissWarning() },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = WarningYellow
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.disclaimer_title),
                    style = MaterialTheme.typography.headlineMedium
                )
            },
            text = {
                Text(
                    text = uiState.errorMessage ?: stringResource(R.string.safety_headphones),
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.dismissWarning() },
                    modifier = Modifier.testTag("dismiss_warning_button")
                ) {
                    Text(stringResource(R.string.disclaimer_accept))
                }
            }
        )
    }

    // Completion Summary Dialog
    if (uiState.isCompleted) {
        AlertDialog(
            onDismissRequest = { viewModel.resetState() },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.auto_done_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Water and dust ejection sequence finished successfully.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Phone volume has been restored to your previous level.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SuccessGreen,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.resetState() },
                    modifier = Modifier.testTag("done_completion_button")
                ) {
                    Text(stringResource(R.string.auto_done))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.resetState()
                        viewModel.onCleanTapped()
                    },
                    modifier = Modifier.testTag("retest_button")
                ) {
                    Text("Clean Again")
                }
            }
        )
    }
}

/**
 * Visual warning banner advising user to maintain distance from ears and orient device downward.
 */
@Composable
private fun SafetyGuidanceCard(
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = WarningYellow.copy(alpha = 0.12f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("safety_guidance_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = WarningYellow,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Keep away from ears · Point speaker downwards",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Safety details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Visual badge indicating volume management state.
 */
@Composable
private fun VolumeStatusCard(
    isVolumeMaximized: Boolean,
    isCleaning: Boolean
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isVolumeMaximized) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("volume_status_badge")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = null,
                tint = if (isVolumeMaximized) DarkAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (isCleaning && isVolumeMaximized) {
                    "Volume set to MAX for ejection · Restores on finish"
                } else {
                    "Volume auto-maximizes during cleaning and restores afterward"
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (isVolumeMaximized) DarkAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Toggle pill between Speaker and Earpiece routing.
 */
@Composable
private fun OutputTogglePill(
    currentTarget: AudioOutputTarget,
    isCleaning: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(4.dp)
            .testTag("output_selector_toggle"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isSpeaker = currentTarget == AudioOutputTarget.SPEAKER

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (isSpeaker) DarkAccent else Color.Transparent)
                .clickable(enabled = !isCleaning && !isSpeaker) { onToggle() }
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = null,
                    tint = if (isSpeaker) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.output_speaker),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSpeaker) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSpeaker) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (!isSpeaker) DarkAccent else Color.Transparent)
                .clickable(enabled = !isCleaning && isSpeaker) { onToggle() }
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Hearing,
                    contentDescription = null,
                    tint = if (!isSpeaker) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.output_earpiece),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (!isSpeaker) FontWeight.Bold else FontWeight.Normal,
                    color = if (!isSpeaker) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Primary 'Clean' trigger button with animated state morphing to 'Stop'.
 */
@Composable
private fun CleanActionButton(
    isCleaning: Boolean,
    onClick: () -> Unit
) {
    val buttonColor by animateColorAsState(
        targetValue = if (isCleaning) DangerRed else DarkAccent,
        animationSpec = tween(durationMillis = 200),
        label = "ButtonColorAnimation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(CircleShape)
            .background(if (isCleaning) buttonColor else Color.Transparent)
            .then(
                if (!isCleaning) {
                    Modifier.background(AccentGradient)
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
            .testTag("clean_button"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isCleaning) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = if (isCleaning) Color.White else Color.Black,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (isCleaning) "Stop" else "Clean Speaker",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = if (isCleaning) Color.White else Color.Black
            )
        }
    }
}
