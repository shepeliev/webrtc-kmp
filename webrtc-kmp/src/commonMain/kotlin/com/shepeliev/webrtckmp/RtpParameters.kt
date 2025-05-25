package com.shepeliev.webrtckmp

public expect class RtpParameters {
    public val codecs: List<RtpCodecParameters>
    public val encodings: List<RtpEncodingParameters>
    public val headerExtension: List<HeaderExtension>
    public val rtcp: RtcpParameters
    public val transactionId: String
}

public expect class RtpCodecParameters {
    /**
     * Payload type used to identify this codec in RTP packets.
     */
    public val payloadType: Int

    /**
     * The codec's MIME media type and subtype specified as a [String] of the form "type/subtype".
     */
    public val mimeType: String?

    /**
     * Clock rate in Hertz.
     */
    public val clockRate: Int?

    /**
     * The number of audio channels used. Set to null for video codecs.
     */
    public val numChannels: Int?

    /**
     * The "format specific parameters" field from the "a=fmtp" line in the SDP
     */
    public val parameters: Map<String, String>
}

public expect class RtpEncodingParameters {
    /**
     * If non-null, this represents the RID that identifies this encoding layer.
     * RIDs are used to identify layers in simulcast.
     */
    public val rid: String?

    /**
     * Set to true to cause this encoding to be sent, and false for it not to
     * be sent.
     */
    public val active: Boolean

    /**
     * The relative bitrate priority of this encoding. Currently this is
     * implemented for the entire RTP sender by using the value of the first
     * encoding parameter. See: https://w3c.github.io/webrtc-priority/#enumdef-rtcprioritytype
     * "very-low" = 0.5
     * "low" = 1.0
     * "medium" = 2.0
     * "high" = 4.0
     */
    public val bitratePriority: Double

    /**
     * The relative DiffServ Code Point priority for this encoding, allowing
     * packets to be marked relatively higher or lower without affecting bandwidth allocations.
     */
    public val networkPriority: Int

    /**
     * If non-null, this represents the Transport Independent Application Specific maximum
     * bandwidth defined in RFC3890. If null, there is no maximum bitrate.
     */
    public val maxBitrateBps: Int?

    /**
     * The minimum bitrate in bps for video.
     */
    public val minBitrateBps: Int?

    /**
     * The max framerate in fps for video.
     */
    public val maxFramerate: Int?

    /**
     * The number of temporal layers for video.
     */
    public val numTemporalLayers: Int?

    /**
     * If non-null, scale the width and height down by this factor for video. If null,
     * implementation default scaling factor will be used.
     */
    public val scaleResolutionDownBy: Double?

    /**
     * SSRC to be used by this encoding.
     * Can't be changed between getParameters/setParameters.
     */
    public val ssrc: Long?
}

public expect class HeaderExtension {
    /**
     * The URI of the RTP header extension, as defined in RFC5285.
     **/
    public val uri: String

    /**
     * The value put in the RTP packet to identify the header extension.
     **/
    public val id: Int

    /**
     * Whether the header extension is encrypted or not.
     **/
    public val encrypted: Boolean
}

public expect class RtcpParameters {
    /**
     * The Canonical Name used by RTCP
     **/
    public val cname: String

    /**
     * Whether reduced size RTCP is configured or compound RTCP
     **/
    public val reducedSize: Boolean
}
