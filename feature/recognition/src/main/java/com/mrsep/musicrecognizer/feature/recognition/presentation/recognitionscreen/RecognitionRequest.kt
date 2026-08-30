package com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen

sealed class RecognitionRequest {
    data object Startup : RecognitionRequest()
    data object Retry : RecognitionRequest()
}