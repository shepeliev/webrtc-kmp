package com.shepeliev.webrtckmp

public expect interface VideoStreamTrack : MediaStreamTrack {
    public suspend fun switchCamera(deviceId: String? = null)
}
