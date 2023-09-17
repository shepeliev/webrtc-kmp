package com.shepeliev.webrtckmp

expect interface VideoStreamTrack : MediaStreamTrack {
    var shouldReceive: Boolean?
    suspend fun switchCamera(deviceId: String? = null)
}
