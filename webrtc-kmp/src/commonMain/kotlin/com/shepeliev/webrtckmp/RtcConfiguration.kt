package com.shepeliev.webrtckmp

data class RtcConfiguration(
    /**
     * Specifies how to handle negotiation of candidates when the remote peer is not compatible
     * with the SDP BUNDLE standard. This must be one of the values from the enum [BundlePolicy].
     * If this value isn't included in the dictionary, "balanced" is assumed.
     */
    val bundlePolicy: BundlePolicy = BundlePolicy.Balanced,

    /**
     * A [List] of objects of type [RtcCertificatePem] which are used by the connection for
     * authentication. If this property isn't specified, a set of certificates is generated
     * automatically for each [PeerConnection] instance. Although only one certificate is used by
     * a given connection, providing certificates for multiple algorithms may improve the odds of
     * successfully connecting in some circumstances. @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/RTCConfiguration/certificates#using_certificates">Using certificates</a>
     * for further information.
     */
    val certificates: List<RtcCertificatePem>? = null,

    /**
     * An unsigned 16-bit integer value which specifies the size of the prefetched ICE candidate
     * pool. The default value is 0 (meaning no candidate prefetching will occur). You may find in
     * some cases that connections can be established more quickly by allowing the ICE agent to
     * start fetching ICE candidates before you start trying to connect, so that they're already
     * available for inspection when [PeerConnection.setLocalDescription] is called.
     */
    val iceCandidatePoolSize: Int = 0,

    /**
     * A [List] of [IceServer] objects, each describing one server which may be used by the ICE
     * agent; these are typically STUN and/or TURN servers. If this isn't specified, the connection
     * attempt will be made with no STUN or TURN server available, which limits the connection to
     * local peers.
     */
    val iceServers: List<IceServer> = emptyList(),

    /**
     * The current ICE transport policy; this must be one of the values from the [IceTransportPolicy]
     * enumeration. If the policy isn't specified, [IceTransportPolicy.All] is assumed by default,
     * allowing all candidates to be considered. A value of [IceTransportPolicy.Relay] limits the
     * candidates to those relayed through another server, such as a STUN or TURN server.
     */
    val iceTransportPolicy: IceTransportPolicy = IceTransportPolicy.All,

    /**
     * The RTCP mux policy to use when gathering ICE candidates, in order to support non-multiplexed
     * RTCP. The value must be one of those from the [RtcpMuxPolicy] enum. The default is
     * [RtcpMuxPolicy.Require].
     */
    val rtcpMuxPolicy: RtcpMuxPolicy = RtcpMuxPolicy.Require,

    val continualGatheringPolicy: ContinualGatheringPolicy = ContinualGatheringPolicy.GatherOnce,
)
