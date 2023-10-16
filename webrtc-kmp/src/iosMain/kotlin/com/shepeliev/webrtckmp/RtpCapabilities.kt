package com.shepeliev.webrtckmp

import WebRTC.RTCRtpCapabilities
import WebRTC.RTCRtpCodecCapability
import WebRTC.RTCRtpHeaderExtension
import WebRTC.RTCRtpMediaType

actual class RtpCapabilities internal constructor(
    val native: RTCRtpCapabilities,
    actual val headerExtensions: List<HeaderExtensionCapability>
) {
    actual val codecs: List<CodecCapability>
        get() = native.codecs.map { CodecCapability(it as RTCRtpCodecCapability) }

    actual class CodecCapability internal constructor(
        val native: RTCRtpCodecCapability
    ) {
        actual val preferredPayloadType: Int? get() = native.preferredPayloadType?.intValue()
        actual val name: String? get() = native.name
        actual val kind: MediaStreamTrackKind get() = native.kind.asCommon()
        actual val clockRate: Int get() = native.clockRate?.intValue() ?: -1
        actual val numChannels: Int? get() = native.numChannels?.intValue()
        actual val parameters: Map<String, String>
            get() = native.parameters.map { "${it.key}" to "${it.value}" }.toMap()
        actual val mimeType: String get() = native.mimeType
    }

    actual class HeaderExtensionCapability internal constructor(
        val native: RTCRtpHeaderExtension
    ) {
        actual val uri: String get() = native.uri
        actual val preferredId: Int? get() = native.id
        actual val preferredEncrypted: Boolean? get() = native.encrypted
    }
}

internal fun RTCRtpMediaType.asCommon(): MediaStreamTrackKind = when (this) {
    RTCRtpMediaType.RTCRtpMediaTypeAudio -> MediaStreamTrackKind.Audio
    RTCRtpMediaType.RTCRtpMediaTypeVideo -> MediaStreamTrackKind.Video
    RTCRtpMediaType.RTCRtpMediaTypeData -> MediaStreamTrackKind.Data
    else -> error("Unknown track kind: $this")
}
