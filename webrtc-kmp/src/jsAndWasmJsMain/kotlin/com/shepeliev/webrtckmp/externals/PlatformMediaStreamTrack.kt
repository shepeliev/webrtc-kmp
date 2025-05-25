package com.shepeliev.webrtckmp.externals

import com.shepeliev.webrtckmp.FacingMode
import com.shepeliev.webrtckmp.MediaStreamTrackState
import com.shepeliev.webrtckmp.MediaTrackConstraints
import com.shepeliev.webrtckmp.MediaTrackSettings
import kotlin.js.JsName

@JsName("MediaStreamTrack")
public external interface PlatformMediaStreamTrack {
    public val id: String
    public var contentHint: String
    public var enabled: Boolean
    public val kind: String
    public val label: String
    public val muted: Boolean
    public var onended: (() -> Unit)?
    public var onmute: (() -> Unit)?
    public var onunmute: (() -> Unit)?
    public val readyState: String

    public fun getSettings(): PlatformMediaTrackSettings

    public fun stop()
}

internal expect fun PlatformMediaStreamTrack.getConstraints(): MediaTrackConstraints

@JsName("MediaTrackSettings")
public external interface PlatformMediaTrackSettings {
    public var aspectRatio: Double?
    public var autoGainControl: Boolean?
    public var channelCount: Int?
    public var deviceId: String?
    public var displaySurface: String?
    public var echoCancellation: Boolean?
    public var facingMode: String?
    public var frameRate: Double?
    public var groupId: String?
    public var height: Int?
    public var noiseSuppression: Boolean?
    public var sampleRate: Int?
    public var sampleSize: Int?
    public var width: Int?
}

internal fun PlatformMediaTrackSettings.asCommon() =
    MediaTrackSettings(
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
        width = width,
    )

internal fun String.toFacingMode() =
    when (this) {
        "user" -> FacingMode.User
        else -> FacingMode.Environment
    }

internal fun String.toMediaStreamTrackState(muted: Boolean) =
    when (this) {
        "live" -> MediaStreamTrackState.Live(muted)
        "ended" -> MediaStreamTrackState.Ended(muted)
        else -> error("Unknown media stream track state: $this")
    }
