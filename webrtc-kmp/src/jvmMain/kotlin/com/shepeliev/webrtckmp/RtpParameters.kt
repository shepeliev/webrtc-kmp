package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.RTCRtcpParameters
import dev.onvoid.webrtc.RTCRtpCodecParameters
import dev.onvoid.webrtc.RTCRtpEncodingParameters
import dev.onvoid.webrtc.RTCRtpHeaderExtensionParameters
import dev.onvoid.webrtc.RTCRtpParameters

actual class RtpParameters(
    val native: RTCRtpParameters,
) {
    actual val codecs: List<RtpCodecParameters>
        get() = native.codecs.map { RtpCodecParameters(it) }

    actual val encodings: List<RtpEncodingParameters>
        get() = emptyList() // TODO

    actual val headerExtension: List<HeaderExtension>
        get() = native.headerExtensions.map { HeaderExtension(it) }

    actual val rtcp: RtcpParameters
        get() = RtcpParameters(native.rtcp)

    actual val transactionId: String
        get() = "" // TODO
}

actual class RtpCodecParameters(val native: RTCRtpCodecParameters) {
    actual val payloadType: Int
        get() = native.payloadType

    actual val mimeType: String?
        get() = "${native.codecName.lowercase()}/${native.mediaType.name.lowercase()}"

    actual val clockRate: Int?
        get() = native.clockRate

    actual val numChannels: Int?
        get() = native.channels

    actual val parameters: Map<String, String>
        get() = native.parameters
}

actual class RtpEncodingParameters(val native: RTCRtpEncodingParameters) {
    actual val rid: String?
        get() = null

    actual val active: Boolean
        get() = native.active

    actual val bitratePriority: Double
        // Referred from [org.webrtc.RtpParameters.Encoding] class
        get() = 1.0

    actual val networkPriority: Int
        // Referred from [org.webrtc.RtpParameters.Encoding] class
        get() = 1

    actual val maxBitrateBps: Int?
        get() = native.maxBitrate

    actual val minBitrateBps: Int?
        get() = native.minBitrate

    actual val maxFramerate: Int?
        get() = native.maxFramerate.toInt()

    actual val numTemporalLayers: Int?
        get() = null

    actual val scaleResolutionDownBy: Double?
        get() = native.scaleResolutionDownBy

    actual val ssrc: Long?
        get() = native.ssrc
}

actual class HeaderExtension(val native: RTCRtpHeaderExtensionParameters) {
    actual val uri: String
        get() = native.uri

    actual val id: Int
        get() = native.id

    actual val encrypted: Boolean
        get() = native.encrypted
}

actual class RtcpParameters(val native: RTCRtcpParameters) {
    actual val cname: String
        get() = native.cName

    actual val reducedSize: Boolean
        get() = native.reducedSize
}
