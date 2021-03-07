package com.shepeliev.webrtckmm

expect object MediaDevices {
    suspend fun getUserMedia(audio: Boolean = true, video: Boolean = false): UserMedia

    suspend fun enumerateDevices(): List<DeviceInfo>

    suspend fun switchCamera(): SwitchCameraResult

    suspend fun switchCamera(cameraId: String): SwitchCameraResult
}

data class DeviceInfo(
    val deviceId: String,
    val label: String,
    val kind: DeviceKind,
    val isFrontFacing: Boolean
)

data class SwitchCameraResult(
    val isFrontCamera: Boolean = false,
    val errorDescription: String? = null
)

enum class DeviceKind { videoInput, audioInput }