package com.shepeliev.webrtckmp

import kotlin.coroutines.cancellation.CancellationException

internal expect class CameraVideoCapturer constructor() {

    @Throws(CameraVideoCapturerException::class)
    fun startCapture(cameraId: String, constraints: VideoConstraints, videoSource: VideoSource)

    @Throws(CameraVideoCapturerException::class, CancellationException::class)
    suspend fun switchCamera(): MediaDeviceInfo

    @Throws(CameraVideoCapturerException::class, CancellationException::class)
    suspend fun switchCamera(cameraId: String): MediaDeviceInfo

    fun stopCapture()
}
