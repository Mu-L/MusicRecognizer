package com.mrsep.musicrecognizer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.core.domain.preferences.AudioCaptureMode
import com.mrsep.musicrecognizer.core.domain.preferences.PreferencesRepository
import com.mrsep.musicrecognizer.core.domain.preferences.ThemeMode
import com.mrsep.musicrecognizer.core.domain.track.TrackRepository
import com.mrsep.musicrecognizer.di.ServiceStarter
import com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen.RecognitionRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.annotation.concurrent.Immutable
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    trackRepository: TrackRepository,
    private val preferencesRepository: PreferencesRepository,
    private val recognitionServiceStarter: ServiceStarter,
) : ViewModel() {

    private val _pendingRecognitionRequest = MutableStateFlow<RecognitionRequest?>(null)
    val pendingRecognitionRequest = _pendingRecognitionRequest.asStateFlow()

    val unviewedTracksCount = trackRepository.getUnviewedCountFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    val uiState = preferencesRepository.userPreferencesFlow
        .map { preferences -> MainActivityUiState.Success(
            onboardingCompleted = preferences.onboardingCompleted,
            recognizeOnStartup = preferences.recognizeOnStartup,
            notificationServiceEnabled = preferences.notificationServiceEnabled,
            dynamicColorsEnabled = preferences.dynamicColorsEnabled,
            themeMode = preferences.themeMode,
            usePureBlackForDarkTheme = preferences.usePureBlackForDarkTheme,
            usePrerecording = preferences.usePrerecording,
            defaultAudioCaptureMode = preferences.defaultAudioCaptureMode,
            mainButtonLongPressAudioCaptureMode = preferences.mainButtonLongPressAudioCaptureMode,
        ) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MainActivityUiState.Loading
        )

    fun setNotificationServiceEnabled(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setNotificationServiceEnabled(value)
            if (value) {
                recognitionServiceStarter.startServiceHoldMode()
            } else {
                recognitionServiceStarter.stopServiceHoldMode()
            }
        }
    }

    fun setPendingRecognitionRequest(request: RecognitionRequest?) {
        when (request) {
            RecognitionRequest.Startup -> requestStartupRecognition(ignoreStartupUserPreference = true)
            RecognitionRequest.Retry,
            null -> _pendingRecognitionRequest.update { request }
        }
    }

    fun requestStartupRecognition(ignoreStartupUserPreference: Boolean) {
        viewModelScope.launch {
            val state = uiState
                .filterIsInstance<MainActivityUiState.Success>()
                .first()
            val shouldRequest = state.onboardingCompleted &&
                    (ignoreStartupUserPreference || state.recognizeOnStartup)
            if (shouldRequest) {
                _pendingRecognitionRequest.update { RecognitionRequest.Startup }
            }
        }
    }
}

@Immutable
sealed class MainActivityUiState {

    data object Loading : MainActivityUiState()

    data class Success(
        val onboardingCompleted: Boolean,
        val recognizeOnStartup: Boolean,
        val notificationServiceEnabled: Boolean,
        val dynamicColorsEnabled: Boolean,
        val themeMode: ThemeMode,
        val usePureBlackForDarkTheme: Boolean,
        val usePrerecording: Boolean,
        val defaultAudioCaptureMode: AudioCaptureMode,
        val mainButtonLongPressAudioCaptureMode: AudioCaptureMode,
    ) : MainActivityUiState()
}
