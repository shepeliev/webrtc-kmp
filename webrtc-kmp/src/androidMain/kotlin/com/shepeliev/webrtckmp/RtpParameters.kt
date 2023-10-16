package com.shepeliev.webrtckmp

import android.util.Log
import org.webrtc.RtpParameters as AndroidRtpParameters

actual class RtpParameters(val native: AndroidRtpParameters) {
    actual val codecs: List<RtpCodecParameters>
        get() = native.codecs.map { RtpCodecParameters(it) }

    actual val encodings: List<RtpEncodingParameters>
        get() = native.encodings.map { RtpEncodingParameters(it) }

    actual val headerExtension: List<HeaderExtension>
        get() = native.headerExtensions.map { HeaderExtension(it) }

    actual val rtcp: RtcpParameters
        get() = RtcpParameters(native.rtcp)

    actual val transactionId: String
        get() = native.transactionId
}

actual class RtpCodecParameters(val native: AndroidRtpParameters.Codec) {
    actual val payloadType: Int
        get() = native.payloadType

    actual val mimeType: String?
        get() = try {
            val kindField = AndroidRtpParameters.Codec::class.java.getField("kind")
            kindField.isAccessible = true
            val kind = kindField.get(native) as? String
            if (kind != null && native.name != null) "$kind/${native.name}" else null
        } catch (e: Throwable) {
            Log.e("RtpCodecParameters", "Getting 'kind' field failed", e)
            null
        }

    actual val clockRate: Int?
        get() = native.clockRate

    actual val numChannels: Int?
        get() = native.numChannels

    actual val parameters: Map<String, String>
        get() = native.parameters
}

actual class RtpEncodingParameters(val native: AndroidRtpParameters.Encoding) {
    actual constructor(rid: String?, active: Boolean, scaleResolutionDownBy: Double?) : this(
        AndroidRtpParameters.Encoding(rid, active, scaleResolutionDownBy)
    )

    actual var rid: String?
        get() = native.rid
        set(value) {
            native.rid = value
        }

    actual var active: Boolean
        get() = native.active
        set(value) {
            native.active = value
        }

    actual var bitratePriority: Double
        get() = native.bitratePriority
        set(value) {
            native.bitratePriority = value
        }

    actual var networkPriority: Priority
        get() = native.networkPriority.asCommon()
        set(value) {
            native.networkPriority = value.toNative()
        }

    actual var maxBitrateBps: Int?
        get() = native.maxBitrateBps
        set(value) {
            native.maxBitrateBps = value
        }

    actual var minBitrateBps: Int?
        get() = native.minBitrateBps
        set(value) {
            native.minBitrateBps = value
        }

    actual var maxFramerate: Int?
        get() = native.maxFramerate
        set(value) {
            native.maxFramerate = value
        }

    actual var numTemporalLayers: Int?
        get() = native.numTemporalLayers
        set(value) {
            native.numTemporalLayers = value
        }

    actual var scaleResolutionDownBy: Double?
        get() = native.scaleResolutionDownBy
        set(value) {
            native.scaleResolutionDownBy = value
        }

    actual val ssrc: Long?
        get() = native.ssrc
}

actual class HeaderExtension(val native: AndroidRtpParameters.HeaderExtension) {
    actual val uri: String
        get() = native.uri

    actual val id: Int
        get() = native.id

    actual val encrypted: Boolean
        get() = native.encrypted
}

actual class RtcpParameters(val native: AndroidRtpParameters.Rtcp) {
    actual val cname: String
        get() = native.cname

    actual val reducedSize: Boolean
        get() = native.reducedSize
}

private fun Priority.toNative(): Int = when (this) {
    Priority.VeryLow -> org.webrtc.Priority.VERY_LOW
    Priority.Low -> org.webrtc.Priority.LOW
    Priority.Medium -> org.webrtc.Priority.MEDIUM
    Priority.High -> org.webrtc.Priority.HIGH
}

private fun Int.asCommon(): Priority {
    return when (this) {
        org.webrtc.Priority.VERY_LOW -> Priority.VeryLow
        org.webrtc.Priority.LOW -> Priority.Low
        org.webrtc.Priority.MEDIUM -> Priority.Medium
        org.webrtc.Priority.HIGH -> Priority.High
        else -> throw IllegalArgumentException("Unknown priority: $this")
    }
}
