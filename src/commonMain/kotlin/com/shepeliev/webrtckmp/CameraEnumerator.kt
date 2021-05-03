package com.shepeliev.webrtckmp

internal expect object CameraEnumerator {
    suspend fun enumerateDevices(): List<MediaDeviceInfo>
    fun createCameraVideoCapturer(source: VideoSource): CameraVideoCapturer
}
