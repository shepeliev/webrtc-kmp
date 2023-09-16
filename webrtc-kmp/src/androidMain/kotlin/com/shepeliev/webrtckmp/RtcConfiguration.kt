package com.shepeliev.webrtckmp

import org.webrtc.PeerConnection

actual class RtcConfiguration actual constructor(
    bundlePolicy: BundlePolicy,
    certificates: List<RtcCertificatePem>?,
    iceCandidatePoolSize: Int,
    iceServers: List<IceServer>,
    iceTransportPolicy: IceTransportPolicy,
    rtcpMuxPolicy: RtcpMuxPolicy,
    continualGatheringPolicy: ContinualGatheringPolicy,
) {
    val android = PeerConnection.RTCConfiguration(iceServers.map { it.native }).apply {
        this.bundlePolicy = bundlePolicy.asNative()
        this.certificate = certificates?.firstOrNull()?.native
        this.iceCandidatePoolSize = iceCandidatePoolSize
        this.iceTransportsType = iceTransportPolicy.asNative()
        this.rtcpMuxPolicy = rtcpMuxPolicy.asNative()
        this.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        this.continualGatheringPolicy = continualGatheringPolicy.asNative()
    }
}

private fun RtcpMuxPolicy.asNative(): PeerConnection.RtcpMuxPolicy {
    return when (this) {
        RtcpMuxPolicy.Negotiate -> PeerConnection.RtcpMuxPolicy.NEGOTIATE
        RtcpMuxPolicy.Require -> PeerConnection.RtcpMuxPolicy.REQUIRE
    }
}

private fun BundlePolicy.asNative(): PeerConnection.BundlePolicy {
    return when (this) {
        BundlePolicy.Balanced -> PeerConnection.BundlePolicy.BALANCED
        BundlePolicy.MaxBundle -> PeerConnection.BundlePolicy.MAXBUNDLE
        BundlePolicy.MaxCompat -> PeerConnection.BundlePolicy.MAXCOMPAT
    }
}

private fun IceTransportPolicy.asNative(): PeerConnection.IceTransportsType {
    return when (this) {
        IceTransportPolicy.None -> PeerConnection.IceTransportsType.NONE
        IceTransportPolicy.Relay -> PeerConnection.IceTransportsType.RELAY
        IceTransportPolicy.NoHost -> PeerConnection.IceTransportsType.NOHOST
        IceTransportPolicy.All -> PeerConnection.IceTransportsType.ALL
    }
}

private fun ContinualGatheringPolicy.asNative(): PeerConnection.ContinualGatheringPolicy {
    return when (this) {
        ContinualGatheringPolicy.GatherOnce -> PeerConnection.ContinualGatheringPolicy.GATHER_ONCE
        ContinualGatheringPolicy.GatherContinually -> PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
    }
}
