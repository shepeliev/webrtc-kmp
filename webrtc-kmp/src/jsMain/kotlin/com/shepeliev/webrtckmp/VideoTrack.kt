package com.shepeliev.webrtckmp

actual interface VideoTrack : MediaStreamTrack {
    actual var shouldReceive: Boolean?
    actual suspend fun switchCamera(deviceId: String?)
}
