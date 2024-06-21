package com.shepeliev.webrtckmp

import com.shepeliev.webrtckmp.externals.RTCRtcpParameters
import com.shepeliev.webrtckmp.externals.RTCRtpCodecParameters
import com.shepeliev.webrtckmp.externals.RTCRtpParameters
import com.shepeliev.webrtckmp.externals.codes

actual class RtpParameters(val platform: RTCRtpParameters) {
    actual val codecs: List<RtpCodecParameters> get() = platform.codes.map { RtpCodecParameters(it) }
    actual val encodings: List<RtpEncodingParameters> get() = emptyList() // TODO
    actual val headerExtension: List<HeaderExtension> get() = emptyList() // TODO
    actual val rtcp: RtcpParameters get() = RtcpParameters(platform.rtcp)
    actual val transactionId: String get() = "" // TODO
}

actual class RtpCodecParameters(val platform: RTCRtpCodecParameters) {
    actual val payloadType: Int
        get() = platform.payloadType ?: 0

    actual val mimeType: String?
        get() = platform.mimeType

    actual val clockRate: Int?
        get() = platform.clockRate

    actual val numChannels: Int?
        get() = platform.channels

    actual val parameters: Map<String, String>
        get() = mapOf("sdpFmtpLine" to "${platform.sdpFmtpLine}") // TODO
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

actual class RtcpParameters(val platform: RTCRtcpParameters) {
    actual val cname: String
        get() = platform.cname

    actual val reducedSize: Boolean
        get() = platform.reducedSize
}
