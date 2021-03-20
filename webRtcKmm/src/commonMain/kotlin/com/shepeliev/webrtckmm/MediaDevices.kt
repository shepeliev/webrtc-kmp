package com.shepeliev.webrtckmm

expect object MediaDevices {
    suspend fun getUserMedia(audio: Boolean = true, video: Boolean = false): MediaStream

    suspend fun enumerateDevices(): List<MediaDeviceInfo>

    suspend fun switchCamera(): MediaDeviceInfo

    suspend fun switchCamera(cameraId: String): MediaDeviceInfo
}

data class MediaDeviceInfo(
    val deviceId: String,
    val label: String,
    val kind: MediaDeviceKind,
    val isFrontFacing: Boolean
)

enum class MediaDeviceKind { VideoInput, AudioInput }