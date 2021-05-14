package com.shepeliev.webrtckmp

data class MediaDeviceInfo(
    val deviceId: String,
    val label: String,
    val kind: MediaDeviceKind,
    val isFrontFacing: Boolean
)

enum class MediaDeviceKind { VideoInput, AudioInput }
