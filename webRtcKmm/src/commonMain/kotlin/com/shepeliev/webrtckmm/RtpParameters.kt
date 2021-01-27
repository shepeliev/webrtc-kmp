package com.shepeliev.webrtckmm

expect class RtpParameters {
    val codecs: List<Codec>
    val encodings: List<Encoding>
    val headerExtension: List<HeaderExtension>
    val rtcp: Rtcp
    val transactionId: String

    class Codec {
        // Payload type used to identify this codec in RTP packets.
        val payloadType: Int

        // Name used to identify the codec. Equivalent to MIME subtype.
        val name: String

        // The media type of this codec. Equivalent to MIME top-level type.
        // val kind: MediaStreamTrack.MediaType

        // Clock rate in Hertz.
        val clockRate: Int?

        // The number of audio channels used. Set to null for video codecs.
        val numChannels: Int?

        // The "format specific parameters" field from the "a=fmtp" line in the SDP
        val parameters: Map<String, String>
    }

    class Encoding {
        // If non-null, this represents the RID that identifies this encoding layer.
        // RIDs are used to identify layers in simulcast.
        val rid: String?

        // Set to true to cause this encoding to be sent, and false for it not to
        // be sent.
        val active: Boolean

        // The relative bitrate priority of this encoding. Currently this is
        // implemented for the entire RTP sender by using the value of the first
        // encoding parameter.
        // See: https://w3c.github.io/webrtc-priority/#enumdef-rtcprioritytype
        // "very-low" = 0.5
        // "low" = 1.0
        // "medium" = 2.0
        // "high" = 4.0
        val bitratePriority: Double

        // The relative DiffServ Code Point priority for this encoding, allowing
        // packets to be marked relatively higher or lower without affecting
        // bandwidth allocations.
        val networkPriority: Int

        // If non-null, this represents the Transport Independent Application
        // Specific maximum bandwidth defined in RFC3890. If null, there is no
        // maximum bitrate.
        val maxBitrateBps: Int?

        // The minimum bitrate in bps for video.
        val minBitrateBps: Int?

        // The max framerate in fps for video.
        val maxFramerate: Int?

        // The number of temporal layers for video.
        val numTemporalLayers: Int?

        // If non-null, scale the width and height down by this factor for video. If null,
        // implementation default scaling factor will be used.
        val scaleResolutionDownBy: Double?

        // SSRC to be used by this encoding.
        // Can't be changed between getParameters/setParameters.
        val ssrc: Long?
    }

    class HeaderExtension {
        /** The URI of the RTP header extension, as defined in RFC5285.  */
        val uri: String

        /** The value put in the RTP packet to identify the header extension.  */
        val id: Int

        /** Whether the header extension is encrypted or not.  */
        val encrypted: Boolean
    }

    class Rtcp {
        /** The Canonical Name used by RTCP  */
        val cname: String

        /** Whether reduced size RTCP is configured or compound RTCP  */
        val reducedSize: Boolean
    }
}
