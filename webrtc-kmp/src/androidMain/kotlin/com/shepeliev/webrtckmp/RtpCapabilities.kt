package com.shepeliev.webrtckmp

import org.webrtc.MediaStreamTrack
import org.webrtc.RtpCapabilities as AndroidRtpCapabilities

actual class RtpCapabilities internal constructor(val native: AndroidRtpCapabilities) {
    actual val codecs: List<CodecCapability>
        get() = native.codecs.map { CodecCapability(it) }
    actual val headerExtensions: List<HeaderExtensionCapability>
        get() = native.headerExtensions.map { HeaderExtensionCapability(it) }

    actual class CodecCapability internal constructor(
        val native: AndroidRtpCapabilities.CodecCapability
    ) {
        actual val preferredPayloadType: Int? get() = native.preferredPayloadType
        actual val name: String? get() = native.name
        actual val kind: MediaStreamTrackKind get() = native.kind.asCommon()
        actual val clockRate: Int get() = native.clockRate
        actual val numChannels: Int? get() = native.numChannels
        actual val parameters: Map<String, String> get() = native.parameters
        actual val mimeType: String get() = native.mimeType
    }

    actual class HeaderExtensionCapability internal constructor(
        val native: AndroidRtpCapabilities.HeaderExtensionCapability
    ) {
        actual val uri: String get() = native.uri
        actual val preferredId: Int? get() = native.preferredId
        actual val preferredEncrypted: Boolean? get() = native.preferredEncrypted
    }
}

internal fun MediaStreamTrack.MediaType.asCommon(): MediaStreamTrackKind = when (this) {
    MediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO -> MediaStreamTrackKind.Audio
    MediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO -> MediaStreamTrackKind.Video
    else -> error("Unknown track kind: $this")
}
