package com.shepeliev.webrtckmp

expect class VideoStreamTrack : MediaStreamTrack {
    suspend fun switchCamera()
    suspend fun switchCamera(deviceId: String)
    override fun stop()
}
