package com.shepeliev.webrtckmp

actual interface VideoStreamTrack : MediaStreamTrack {
    actual var shouldReceive: Boolean?
    actual suspend fun switchCamera(deviceId: String?)
}
