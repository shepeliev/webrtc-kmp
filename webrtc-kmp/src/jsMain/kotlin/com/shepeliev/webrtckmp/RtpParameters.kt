package com.shepeliev.webrtckmp

import kotlin.js.Json
import kotlin.js.json

actual class RtpParameters(val js: RTCRtpParameters) {
    actual val codecs: List<RtpCodecParameters>
        get() = js.codes.map { RtpCodecParameters(it) }

    actual val encodings: List<RtpEncodingParameters>
        get() = emptyList() // TODO

    actual val headerExtension: List<HeaderExtension>
        get() = emptyList() // TODO

    actual val rtcp: RtcpParameters
        get() = RtcpParameters(js.rtcp)

    actual val transactionId: String
        get() = "" // TODO
}

actual class RtpCodecParameters(val js: RTCRtpCodecParameters) {
    actual val payloadType: Int
        get() = js.payloadType ?: 0

    actual val mimeType: String?
        get() = js.mimeType

    actual val clockRate: Int?
        get() = js.clockRate

    actual val numChannels: Int?
        get() = js.channels

    actual val parameters: Map<String, String>
        get() = mapOf("sdpFmtpLine" to "${js.sdpFmtpLine}") // TODO
}

actual class RtcpParameters(val js: RTCRtcpParameters) {
    actual val cname: String
        get() = js.cname

    actual val reducedSize: Boolean
        get() = js.reducedSize
}

actual class RtpEncodingParameters(val native: Json) {
    actual constructor(rid: String?, active: Boolean, scaleResolutionDownBy: Double?) : this(
        json("rid" to rid, "active" to active, "scaleResolutionDownBy" to scaleResolutionDownBy)
    )

    actual var rid: String?
        get() = native["rid"] as String?
        set(value) {
            native["rid"] = value
        }

    actual var active: Boolean
        get() = native["active"] as Boolean
        set(value) {
            native["active"] = value
        }

    actual var bitratePriority: Double
        get() = 0.0
        set(value) {
            // not implemented in js
        }


    actual var networkPriority: Priority
        get() = (native["priority"] as String?)?.toPriority() ?: Priority.Low
        set(value) {
            native["priority"] = value.toJsPriority()
        }

    actual var maxBitrateBps: Int?
        get() = native["maxBitrateBps"] as Int?
        set(value) {
            native["maxBitrateBps"] = value
        }

    actual var minBitrateBps: Int?
        get() = null
        set(value) {
            // not implemented in js
        }

    actual var maxFramerate: Int?
        get() = native["maxFramerate"] as Int?
        set(value) {
            native["maxFramerate"] = value
        }

    actual var numTemporalLayers: Int?
        get() = null
        set(value) {
            // not implemented in js
        }

    actual var scaleResolutionDownBy: Double?
        get() = native["scaleResolutionDownBy"] as Double?
        set(value) {
            native["scaleResolutionDownBy"] = value
        }

    actual val ssrc: Long? get() = native["ssrc"] as Long?
}

actual class HeaderExtension {
    actual val uri: String = ""
    actual val id: Int = -1
    actual val encrypted: Boolean = false
}

private fun Priority.toJsPriority(): String = when (this) {
    Priority.VeryLow -> "very-low"
    Priority.Low -> "low"
    Priority.Medium -> "medium"
    Priority.High -> "high"
}

private fun String.toPriority(): Priority = when (this) {
    "very-low" -> Priority.VeryLow
    "low" -> Priority.Low
    "medium" -> Priority.Medium
    "high" -> Priority.High
    else -> throw IllegalArgumentException("Unknown priority: $this")
}
