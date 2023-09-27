package com.shepeliev.webrtckmp

import WebRTC.RTCPriority
import WebRTC.RTCRtcpParameters
import WebRTC.RTCRtpCodecParameters
import WebRTC.RTCRtpEncodingParameters
import WebRTC.RTCRtpHeaderExtension
import WebRTC.RTCRtpParameters
import platform.Foundation.NSNumber

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
    actual constructor(rid: String?, active: Boolean, scaleResolutionDownBy: Double?) : this(
        RTCRtpEncodingParameters().apply {
            this.rid = rid
            this.isActive = active
            this.scaleResolutionDownBy =  scaleResolutionDownBy?.let { NSNumber(it) }
        }
    )

    actual var rid: String?
        get() = native.rid
        set(value) {
            native.rid = value
        }

    actual var active: Boolean
        get() = native.isActive
        set(value) {
            native.isActive = value
        }

    actual var bitratePriority: Double
        get() {
            // not implemented
            return native.bitratePriority
        }
        set(value) {
            native.bitratePriority = value
        }

    actual var networkPriority: Priority
        get() = native.networkPriority.toPriority()
        set(value) {
            native.networkPriority = value.toRTCPriority()
        }

    actual var maxBitrateBps: Int?
        get() = native.maxBitrateBps?.intValue
        set(value) {
            native.maxBitrateBps = value?.let { NSNumber(it) }
        }

    actual var minBitrateBps: Int?
        get() = native.minBitrateBps?.intValue
        set(value) {
            native.minBitrateBps = value?.let { NSNumber(it) }
        }

    actual var maxFramerate: Int?
        get() = native.maxFramerate?.intValue
        set(value) {
            native.maxFramerate = value?.let { NSNumber(it) }
        }

    actual var numTemporalLayers: Int?
        get() = native.numTemporalLayers?.intValue
        set(value) {
            native.numTemporalLayers = value?.let { NSNumber(it) }
        }

    actual var scaleResolutionDownBy: Double?
        get() = native.scaleResolutionDownBy?.doubleValue
        set(value) {
            native.scaleResolutionDownBy = value?.let { NSNumber(it) }
        }

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

private fun Priority.toRTCPriority(): RTCPriority = when (this) {
    Priority.VeryLow -> RTCPriority.RTCPriorityVeryLow
    Priority.Low -> RTCPriority.RTCPriorityLow
    Priority.Medium -> RTCPriority.RTCPriorityMedium
    Priority.High -> RTCPriority.RTCPriorityHigh
}

private fun RTCPriority.toPriority(): Priority = when (this) {
    RTCPriority.RTCPriorityVeryLow -> Priority.VeryLow
    RTCPriority.RTCPriorityLow -> Priority.Low
    RTCPriority.RTCPriorityMedium -> Priority.Medium
    RTCPriority.RTCPriorityHigh -> Priority.High
    else -> throw IllegalArgumentException("Unknown RTCPriority: $this")
}
