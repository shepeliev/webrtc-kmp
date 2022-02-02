package com.shepeliev.webrtckmp

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

actual class RtpEncodingParameters {
    actual val rid: String? = null
    actual val active: Boolean = false
    actual val bitratePriority: Double = 0.0
    actual val networkPriority: Int = -1
    actual val maxBitrateBps: Int? = null
    actual val minBitrateBps: Int? = null
    actual val maxFramerate: Int? = null
    actual val numTemporalLayers: Int? = null
    actual val scaleResolutionDownBy: Double? = null
    actual val ssrc: Long? = null
}

actual class HeaderExtension {
    actual val uri: String = ""
    actual val id: Int = -1
    actual val encrypted: Boolean = false
}
