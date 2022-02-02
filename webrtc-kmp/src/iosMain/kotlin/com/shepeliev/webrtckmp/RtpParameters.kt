package com.shepeliev.webrtckmp

import WebRTC.RTCRtcpParameters
import WebRTC.RTCRtpCodecParameters
import WebRTC.RTCRtpEncodingParameters
import WebRTC.RTCRtpHeaderExtension
import WebRTC.RTCRtpParameters

actual class RtpParameters(val native: RTCRtpParameters) {
    actual val codecs: List<RtpCodecParameters>
        get() = native.codecs.map { RtpCodecParameters(it as RTCRtpCodecParameters) }

    actual val encodings: List<RtpEncodingParameters>
        get() = native.encodings.map { RtpEncodingParameters(it as RTCRtpEncodingParameters) }

    actual val headerExtension: List<HeaderExtension>
        get() = native.headerExtensions.map { HeaderExtension(it as RTCRtpHeaderExtension) }

    actual val rtcp: RtcpParameters
        get() = RtcpParameters(native.rtcp)

    actual val transactionId: String
        get() = native.transactionId
}

actual class RtpCodecParameters(val native: RTCRtpCodecParameters) {
    actual val payloadType: Int
        get() = native.payloadType

    actual val mimeType: String?
        get() = "${native.kind}/${native.name}"

    actual val clockRate: Int?
        get() = native.clockRate?.intValue

    actual val numChannels: Int?
        get() = native.numChannels?.intValue

    actual val parameters: Map<String, String>
        get() = native.parameters.map { (k, v) -> "$k" to "$v" }.toMap()
}

actual class RtpEncodingParameters(val native: RTCRtpEncodingParameters) {
    actual val rid: String?
        get() = native.rid

    actual val active: Boolean
        get() = native.isActive

    actual val bitratePriority: Double
        get() {
            // not implemented
            return 0.0
        }

    actual val networkPriority: Int
        get() {
            // not implemented
            return 0
        }

    actual val maxBitrateBps: Int?
        get() = native.maxBitrateBps?.intValue

    actual val minBitrateBps: Int?
        get() = native.minBitrateBps?.intValue

    actual val maxFramerate: Int?
        get() = native.maxFramerate?.intValue

    actual val numTemporalLayers: Int?
        get() = native.numTemporalLayers?.intValue

    actual val scaleResolutionDownBy: Double?
        get() = native.scaleResolutionDownBy?.doubleValue

    actual val ssrc: Long?
        get() = native.ssrc?.longValue
}

actual class HeaderExtension(val native: RTCRtpHeaderExtension) {
    actual val uri: String
        get() = native.uri

    actual val id: Int
        get() = native.id

    actual val encrypted: Boolean
        get() = native.encrypted
}

actual class RtcpParameters(val native: RTCRtcpParameters) {
    actual val cname: String
        get() = native.cname

    actual val reducedSize: Boolean
        get() = native.isReducedSize
}
