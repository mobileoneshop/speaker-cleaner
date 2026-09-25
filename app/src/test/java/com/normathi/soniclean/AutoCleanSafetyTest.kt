package com.normathi.soniclean

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.normathi.soniclean.viewmodel.AutoCleanViewModel
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AutoCleanSafetyTest {

    @Test
    fun safety_dialog_is_shown_on_first_clean_tap() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        // Clear prefs for fresh test
        app.getSharedPreferences("sonic_clean_prefs", Application.MODE_PRIVATE).edit().clear().commit()

        val viewModel = AutoCleanViewModel(app)
        assertFalse(viewModel.uiState.value.showSafetyDialog)
        assertFalse(viewModel.uiState.value.isCleaning)

        // User taps clean
        viewModel.onCleanTapped()

        // Should prompt safety first dialog
        assertTrue(viewModel.uiState.value.showSafetyDialog)
        assertFalse(viewModel.uiState.value.isCleaning)

        // User cancels
        viewModel.onSafetyDismissed()
        assertFalse(viewModel.uiState.value.showSafetyDialog)
        assertFalse(viewModel.uiState.value.isCleaning)

        // User taps again and confirms with dontShowAgain = true
        viewModel.onCleanTapped()
        assertTrue(viewModel.uiState.value.showSafetyDialog)

        viewModel.onSafetyConfirmed(dontShowAgain = true)
        assertFalse(viewModel.uiState.value.showSafetyDialog)
        assertTrue(viewModel.uiState.value.isCleaning)

        viewModel.stopCleaning()
        assertFalse(viewModel.uiState.value.isCleaning)

        // Next time clean is tapped, it starts directly without dialog because it was acknowledged
        viewModel.onCleanTapped()
        assertFalse(viewModel.uiState.value.showSafetyDialog)
        assertTrue(viewModel.uiState.value.isCleaning)

        viewModel.stopCleaning()
    }
}
