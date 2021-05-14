package com.shepeliev.webrtckmp

internal expect object CameraEnumerator {
    suspend fun enumerateDevices(): List<MediaDeviceInfo>
    fun selectDevice(constraints: VideoConstraints): MediaDeviceInfo
}
