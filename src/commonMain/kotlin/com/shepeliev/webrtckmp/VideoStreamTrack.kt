package com.shepeliev.webrtckmp

expect class VideoStreamTrack : MediaStreamTrack {
    suspend fun switchCamera(deviceId: String? = null)
}
