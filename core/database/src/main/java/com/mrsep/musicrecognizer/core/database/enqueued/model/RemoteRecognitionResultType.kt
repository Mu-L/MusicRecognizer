package com.mrsep.musicrecognizer.core.database.enqueued.model

enum class RemoteRecognitionResultType {
    Success,
    NoMatches,
    NoSoundDetected,
    BadConnection,
    BadRecording,
    AuthError,
    ApiUsageLimited,
    HttpError,
    UnhandledError
}
