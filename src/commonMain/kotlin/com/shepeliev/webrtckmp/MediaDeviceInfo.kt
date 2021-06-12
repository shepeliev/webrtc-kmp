package com.shepeliev.webrtckmp

data class MediaDeviceInfo(
    val deviceId: String,
    val label: String,
    val kind: MediaDeviceKind,
)

enum class MediaDeviceKind { VideoInput, AudioInput }
