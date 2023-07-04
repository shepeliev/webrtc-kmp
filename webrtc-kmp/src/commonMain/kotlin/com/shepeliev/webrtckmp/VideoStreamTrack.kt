package com.shepeliev.webrtckmp

expect interface VideoStreamTrack : MediaStreamTrack {
    suspend fun switchCamera(deviceId: String? = null)
}
