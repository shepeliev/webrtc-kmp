package com.shepeliev.webrtckmp.externals

import kotlin.js.JsName
import com.shepeliev.webrtckmp.FacingMode
import com.shepeliev.webrtckmp.MediaStreamTrackState
import com.shepeliev.webrtckmp.MediaTrackConstraints
import com.shepeliev.webrtckmp.MediaTrackSettings

@JsName("MediaStreamTrack")
external interface PlatformMediaStreamTrack {
    val id: String
    var contentHint: String
    var enabled: Boolean
    val kind: String
    val label: String
    val muted: Boolean
    var onended: (() -> Unit)?
    var onmute: (() -> Unit)?
    var onunmute: (() -> Unit)?
    val readyState: String

    fun getSettings(): PlatformMediaTrackSettings
    fun stop()
}

internal expect fun PlatformMediaStreamTrack.getConstraints(): MediaTrackConstraints

@JsName("MediaTrackSettings")
external interface PlatformMediaTrackSettings {
    var aspectRatio: Double?
    var autoGainControl: Boolean?
    var channelCount: Int?
    var deviceId: String?
    var displaySurface: String?
    var echoCancellation: Boolean?
    var facingMode: String?
    var frameRate: Double?
    var groupId: String?
    var height: Int?
    var noiseSuppression: Boolean?
    var sampleRate: Int?
    var sampleSize: Int?
    var width: Int?
}

internal fun PlatformMediaTrackSettings.asCommon() = MediaTrackSettings(
    aspectRatio = aspectRatio,
    autoGainControl = autoGainControl,
    channelCount = channelCount,
    deviceId = deviceId,
    echoCancellation = echoCancellation,
    facingMode = facingMode?.toFacingMode(),
    frameRate = frameRate,
    groupId = groupId,
    height = height,
    noiseSuppression = noiseSuppression,
    sampleRate = sampleRate,
    sampleSize = sampleSize,
    width = width
)

internal fun String.toFacingMode() = when (this) {
    "user" -> FacingMode.User
    else -> FacingMode.Environment
}

internal fun String.toMediaStreamTrackState(muted: Boolean) = when (this) {
    "live" -> MediaStreamTrackState.Live(muted)
    "ended" -> MediaStreamTrackState.Ended(muted)
    else -> error("Unknown media stream track state: $this")
}
