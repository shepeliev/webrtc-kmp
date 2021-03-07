package com.shepeliev.webrtckmm

actual object MediaDevices {
    actual suspend fun getUserMedia(audio: Boolean, video: Boolean): UserMedia {
        TODO()
    }

    actual suspend fun enumerateDevices(): List<DeviceInfo> {
        TODO()
    }

    actual suspend fun switchCamera(): SwitchCameraResult {
        TODO()
    }

    actual suspend fun switchCamera(cameraId: String): SwitchCameraResult {
        TODO()
    }
}
