package com.shepeliev.webrtckmp

import com.shepeliev.webrtckmp.externals.RTCRtcpParameters
import com.shepeliev.webrtckmp.externals.RTCRtpCodecParameters
import com.shepeliev.webrtckmp.externals.RTCRtpParameters
import com.shepeliev.webrtckmp.externals.codes

public actual class RtpParameters internal constructor(
    internal val platform: RTCRtpParameters,
) {
    public actual val codecs: List<RtpCodecParameters> get() =
        platform.codes.map {
            RtpCodecParameters(it)
        }
    public actual val encodings: List<RtpEncodingParameters> get() = emptyList() // TODO
    public actual val headerExtension: List<HeaderExtension> get() = emptyList() // TODO
    public actual val rtcp: RtcpParameters get() = RtcpParameters(platform.rtcp)
    public actual val transactionId: String get() = "" // TODO
}

public actual class RtpCodecParameters internal constructor(
    internal val platform: RTCRtpCodecParameters,
) {
    public actual val payloadType: Int get() = platform.payloadType ?: 0
    public actual val mimeType: String? get() = platform.mimeType
    public actual val clockRate: Int? get() = platform.clockRate
    public actual val numChannels: Int? get() = platform.channels
    public actual val parameters: Map<String, String>
        get() = mapOf("sdpFmtpLine" to "${platform.sdpFmtpLine}") // TODO
}

public actual class RtpEncodingParameters {
    public actual val rid: String? = null
    public actual val active: Boolean = false
    public actual val bitratePriority: Double = 0.0
    public actual val networkPriority: Int = -1
    public actual val maxBitrateBps: Int? = null
    public actual val minBitrateBps: Int? = null
    public actual val maxFramerate: Int? = null
    public actual val numTemporalLayers: Int? = null
    public actual val scaleResolutionDownBy: Double? = null
    public actual val ssrc: Long? = null
}

public actual class HeaderExtension {
    public actual val uri: String = ""
    public actual val id: Int = -1
    public actual val encrypted: Boolean = false
}

public actual class RtcpParameters internal constructor(
    internal val platform: RTCRtcpParameters,
) {
    public actual val cname: String get() = platform.cname
    public actual val reducedSize: Boolean get() = platform.reducedSize
}
