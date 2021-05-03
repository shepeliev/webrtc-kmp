package com.shepeliev.webrtckmp

import org.webrtc.RtpParameters as NativeRtpParameters

actual class RtpParameters(val native: NativeRtpParameters) {
    actual val codecs: List<Codec>
        get() = native.codecs.map { Codec(it) }

    actual val encodings: List<Encoding>
        get() = native.encodings.map { Encoding(it) }

    actual val headerExtension: List<HeaderExtension>
        get() = native.headerExtensions.map { HeaderExtension(it) }

    actual val rtcp: Rtcp
        get() = Rtcp(native.rtcp)

    actual val transactionId: String
        get() = native.transactionId

    actual class Codec(val native: NativeRtpParameters.Codec) {
        actual val payloadType: Int
            get() = native.payloadType

        actual val name: String
            get() = native.name

        actual val clockRate: Int?
            get() = native.clockRate

        actual val numChannels: Int?
            get() = native.numChannels

        actual val parameters: Map<String, String>
            get() = native.parameters
    }

    actual class Encoding(val native: NativeRtpParameters.Encoding) {
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

    actual class HeaderExtension(val native: NativeRtpParameters.HeaderExtension) {
        /** The URI of the RTP header extension, as defined in RFC5285.  */
        actual val uri: String
            get() = native.uri

        /** The value put in the RTP packet to identify the header extension.  */
        actual val id: Int
            get() = native.id

        /** Whether the header extension is encrypted or not.  */
        actual val encrypted: Boolean
            get() = native.encrypted

    }

    actual class Rtcp(val native: NativeRtpParameters.Rtcp) {
        /** The Canonical Name used by RTCP  */
        actual val cname: String
            get() = native.cname

        /** Whether reduced size RTCP is configured or compound RTCP  */
        actual val reducedSize: Boolean
            get() = native.reducedSize
    }
}
