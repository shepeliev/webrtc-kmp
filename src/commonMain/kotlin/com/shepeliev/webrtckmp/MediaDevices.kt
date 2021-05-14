package com.shepeliev.webrtckmp

import kotlin.coroutines.cancellation.CancellationException

expect object MediaDevices {
    @Throws(CameraVideoCapturerException::class, CancellationException::class)
    suspend fun getUserMedia(audio: Boolean, video: Boolean): MediaStream

    suspend fun enumerateDevices(): List<MediaDeviceInfo>

    @Throws(CameraVideoCapturerException::class, CancellationException::class)
    suspend fun switchCamera(): MediaDeviceInfo

    @Throws(CameraVideoCapturerException::class, CancellationException::class)
    suspend fun switchCamera(cameraId: String): MediaDeviceInfo
}
