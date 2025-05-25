package com.shepeliev.webrtckmp

public data class MediaDeviceInfo(
    val deviceId: String,
    val label: String,
    val kind: MediaDeviceKind,
)

public enum class MediaDeviceKind { VideoInput, AudioInput }
