package com.shepeliev.webrtckmp

actual class RtpCapabilities internal constructor(
    val native: RTCRtpCapabilities,
    private val kind: MediaStreamTrackKind,
) {
    actual val codecs: List<CodecCapability>
        get() = native.codecs.map { CodecCapability(it, kind) }

    actual val headerExtensions: List<HeaderExtensionCapability>
        get() = native.headerExtensions.map { HeaderExtensionCapability(it) }
    actual class CodecCapability internal constructor(
        val native: RTCRtpCodecCapability,
        actual val kind: MediaStreamTrackKind,
    ) {
        actual val preferredPayloadType: Int? = null
        actual val name: String? get() = null
        actual val clockRate: Int get() = native.clockRate
        actual val numChannels: Int? get() = native.channels

        // TODO fill parameters
        actual val parameters: Map<String, String> = emptyMap()
        actual val mimeType: String get() = native.mimeType
    }

    actual class HeaderExtensionCapability internal constructor(
        val native: RTCRtpHeaderExtension
    ) {
        actual val uri: String get() = native.uri
        actual val preferredId: Int? = null
        actual val preferredEncrypted: Boolean? = null
    }
}
