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
    actual val rid: String?
        get() = native.rid

    actual val active: Boolean
        get() = native.active

    actual val bitratePriority: Double
        get() = native.bitratePriority

    actual val networkPriority: Int
        get() = native.networkPriority

    actual val maxBitrateBps: Int?
        get() = native.maxBitrateBps

    actual val minBitrateBps: Int?
        get() = native.minBitrateBps

    actual val maxFramerate: Int?
        get() = native.maxFramerate

    actual val numTemporalLayers: Int?
        get() = native.numTemporalLayers

    actual val scaleResolutionDownBy: Double?
        get() = native.scaleResolutionDownBy

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
