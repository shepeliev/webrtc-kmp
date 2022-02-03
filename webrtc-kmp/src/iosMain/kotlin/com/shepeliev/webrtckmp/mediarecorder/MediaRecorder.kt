package com.shepeliev.webrtckmp.mediarecorder

import com.shepeliev.webrtckmp.MediaStream
import kotlinx.coroutines.flow.Flow

actual class MediaRecorder actual constructor(
    private val stream: MediaStream,
    private val options: MediaRecorderOptions
) {
    actual val onDataAvailable: Flow<String>
        get() = TODO()

    actual val onError: Flow<Throwable>
        get() = TODO()

    actual val onPause: Flow<Unit>
        get() = TODO()

    actual val onResume: Flow<Unit>
        get() = TODO()

    actual val onStart: Flow<Unit>
        get() = TODO()

    actual val onStop: Flow<Unit>
        get() = TODO()

    actual val state: MediaRecorderState
        get() = TODO()

    actual fun pause() {
        TODO()
    }

    actual fun requestData() {
        TODO()
    }

    actual fun resume() {
        TODO()
    }

    actual fun start(timeSliceMillis: Long) {
        TODO()
    }

    actual fun stop() {
        TODO()
    }
}
