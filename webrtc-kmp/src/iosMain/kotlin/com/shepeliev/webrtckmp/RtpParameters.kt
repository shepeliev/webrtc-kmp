@file:OptIn(ExperimentalForeignApi::class)

package com.shepeliev.webrtckmp

import WebRTC.RTCRtcpParameters
import WebRTC.RTCRtpCodecParameters
import WebRTC.RTCRtpEncodingParameters
import WebRTC.RTCRtpHeaderExtension
import WebRTC.RTCRtpParameters
import kotlinx.cinterop.ExperimentalForeignApi

public actual class RtpParameters(
    public val native: RTCRtpParameters,
) {
    public actual val codecs: List<RtpCodecParameters>
        get() = native.codecs.map { RtpCodecParameters(it as RTCRtpCodecParameters) }

    public actual val encodings: List<RtpEncodingParameters>
        get() = native.encodings.map { RtpEncodingParameters(it as RTCRtpEncodingParameters) }

    public actual val headerExtension: List<HeaderExtension>
        get() = native.headerExtensions.map { HeaderExtension(it as RTCRtpHeaderExtension) }

    public actual val rtcp: RtcpParameters get() = RtcpParameters(native.rtcp)
    public actual val transactionId: String get() = native.transactionId
}

public actual class RtpCodecParameters(
    public val native: RTCRtpCodecParameters,
) {
    public actual val payloadType: Int get() = native.payloadType
    public actual val mimeType: String? get() = "${native.kind}/${native.name}"
    public actual val clockRate: Int? get() = native.clockRate?.intValue
    public actual val numChannels: Int? get() = native.numChannels?.intValue
    public actual val parameters: Map<String, String>
        get() = native.parameters.map { (k, v) -> "$k" to "$v" }.toMap()
}

public actual class RtpEncodingParameters(
    public val native: RTCRtpEncodingParameters,
) {
    public actual val rid: String?
        get() = native.rid

    public actual val active: Boolean
        get() = native.isActive

    public actual val bitratePriority: Double
        get() {
            // not implemented
            return 0.0
        }

    public actual val networkPriority: Int
        get() {
            // not implemented
            return 0
        }

    public actual val maxBitrateBps: Int? get() = native.maxBitrateBps?.intValue
    public actual val minBitrateBps: Int? get() = native.minBitrateBps?.intValue
    public actual val maxFramerate: Int? get() = native.maxFramerate?.intValue
    public actual val numTemporalLayers: Int? get() = native.numTemporalLayers?.intValue

    public actual val scaleResolutionDownBy: Double? get() =
        native.scaleResolutionDownBy
            ?.doubleValue

    public actual val ssrc: Long? get() = native.ssrc?.longValue
}

public actual class HeaderExtension(
    public val native: RTCRtpHeaderExtension,
) {
    public actual val uri: String get() = native.uri
    public actual val id: Int get() = native.id
    public actual val encrypted: Boolean get() = native.encrypted
}

public actual class RtcpParameters(
    public val native: RTCRtcpParameters,
) {
    public actual val cname: String get() = native.cname
    public actual val reducedSize: Boolean get() = native.isReducedSize
}
