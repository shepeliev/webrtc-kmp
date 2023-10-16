package com.shepeliev.webrtckmp

expect interface VideoTrack : MediaStreamTrack {
    var shouldReceive: Boolean?
    suspend fun switchCamera(deviceId: String? = null)
}

@Deprecated("Use VideoTrack instead", ReplaceWith("VideoTrack"))
typealias VideoStreamTrack = VideoTrack
