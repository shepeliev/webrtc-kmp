package com.shepeliev.webrtckmp

actual interface VideoStreamTrack : MediaStreamTrack {
    actual suspend fun switchCamera(deviceId: String?)
}
