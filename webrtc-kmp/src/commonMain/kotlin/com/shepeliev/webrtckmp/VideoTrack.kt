package com.shepeliev.webrtckmp

public expect interface VideoTrack : MediaStreamTrack {
    public suspend fun switchCamera(deviceId: String? = null)
}

@Deprecated("Use VideoTrack instead", ReplaceWith("VideoTrack"))
public typealias VideoStreamTrack = VideoTrack
