package com.shepeliev.webrtckmp

actual object MediaDevices {
    actual suspend fun getUserMedia(audio: Boolean, video: Boolean): MediaStream {
        return PhoneMediaDevices.getUserMedia(audio, video)
    }

    actual suspend fun enumerateDevices() = CameraEnumerator.enumerateDevices()

    actual suspend fun switchCamera() = PhoneMediaDevices.switchCamera()

    actual suspend fun switchCamera(cameraId: String) = PhoneMediaDevices.switchCamera(cameraId)
}
