package com.shepeliev.webrtckmp

import kotlin.coroutines.cancellation.CancellationException

internal interface CameraVideoCapturer {

    @Throws(CameraVideoCapturerException::class)
    fun startCapture(constraints: VideoConstraints): MediaDeviceInfo

    @Throws(CameraVideoCapturerException::class, CancellationException::class)
    suspend fun switchCamera(): MediaDeviceInfo

    @Throws(CameraVideoCapturerException::class, CancellationException::class)
    suspend fun switchCamera(cameraId: String): MediaDeviceInfo

    fun stopCapture()
}
