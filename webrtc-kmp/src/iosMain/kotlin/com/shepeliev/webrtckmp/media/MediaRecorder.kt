package com.shepeliev.webrtckmp.media

import com.shepeliev.webrtckmp.MediaStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

actual class MediaRecorder actual constructor(
    stream: MediaStream,
    options: MediaRecorderOptions
) {
    actual val state: StateFlow<MediaRecorderState>
        get() = TODO("Not yet implemented")
    actual val onDataAvailable: Flow<String>
        get() = TODO("Not yet implemented")

    actual suspend fun start() {
    }

    actual fun stop() {
    }
}
