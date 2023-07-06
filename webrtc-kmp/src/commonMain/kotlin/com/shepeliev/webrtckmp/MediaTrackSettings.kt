package com.shepeliev.webrtckmp

data class MediaTrackSettings(
    val aspectRatio: Double? = null,
    val autoGainControl: Boolean? = null,
    val channelCount: Int? = null,
    val deviceId: String? = null,
    val echoCancellation: Boolean? = null,
    val facingMode: FacingMode? = null,
    val frameRate: Double? = null,
    val groupId: String? = null,
    val height: Int? = null,
    val latency: Double? = null,
    val noiseSuppression: Boolean? = null,
    val sampleRate: Int? = null,
    val sampleSize: Int? = null,
    val width: Int? = null,
)
