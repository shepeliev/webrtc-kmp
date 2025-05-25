package com.shepeliev.webrtckmp

import android.util.Log
import org.webrtc.RtpParameters as AndroidRtpParameters

public actual class RtpParameters(
    public val native: AndroidRtpParameters,
) {
    public actual val codecs: List<RtpCodecParameters>
        get() = native.codecs.map { RtpCodecParameters(it) }

    public actual val encodings: List<RtpEncodingParameters>
        get() = native.encodings.map { RtpEncodingParameters(it) }

    public actual val headerExtension: List<HeaderExtension>
        get() = native.headerExtensions.map { HeaderExtension(it) }

    public actual val rtcp: RtcpParameters get() = RtcpParameters(native.rtcp)
    public actual val transactionId: String get() = native.transactionId
}

public actual class RtpCodecParameters(
    public val native: AndroidRtpParameters.Codec,
) {
    public actual val payloadType: Int get() = native.payloadType

    public actual val mimeType: String?
        get() =
            try {
                val kindField = AndroidRtpParameters.Codec::class.java.getField("kind")
                kindField.isAccessible = true
                val kind = kindField.get(native) as? String
                if (kind != null && native.name != null) "$kind/${native.name}" else null
            } catch (e: Throwable) {
                Log.e("RtpCodecParameters", "Getting 'kind' field failed", e)
                null
            }

    public actual val clockRate: Int? get() = native.clockRate
    public actual val numChannels: Int? get() = native.numChannels
    public actual val parameters: Map<String, String> get() = native.parameters
}

public actual class RtpEncodingParameters(
    public val native: AndroidRtpParameters.Encoding,
) {
    public actual val rid: String? get() = native.rid
    public actual val active: Boolean get() = native.active
    public actual val bitratePriority: Double get() = native.bitratePriority
    public actual val networkPriority: Int get() = native.networkPriority
    public actual val maxBitrateBps: Int? get() = native.maxBitrateBps
    public actual val minBitrateBps: Int? get() = native.minBitrateBps
    public actual val maxFramerate: Int? get() = native.maxFramerate
    public actual val numTemporalLayers: Int? get() = native.numTemporalLayers
    public actual val scaleResolutionDownBy: Double? get() = native.scaleResolutionDownBy
    public actual val ssrc: Long? get() = native.ssrc
}

public actual class HeaderExtension(
    public val native: AndroidRtpParameters.HeaderExtension,
) {
    public actual val uri: String get() = native.uri
    public actual val id: Int get() = native.id
    public actual val encrypted: Boolean get() = native.encrypted
}

public actual class RtcpParameters(
    public val native: AndroidRtpParameters.Rtcp,
) {
    public actual val cname: String get() = native.cname
    public actual val reducedSize: Boolean get() = native.reducedSize
}
