package com.shepeliev.webrtckmp

import org.webrtc.PeerConnection

actual class RtcConfiguration actual constructor(
    bundlePolicy: BundlePolicy,
    certificates: List<RtcCertificatePem>?,
    iceCandidatePoolSize: Int,
    iceServers: List<IceServer>,
    iceTransportPolicy: IceTransportPolicy,
    rtcpMuxPolicy: RtcpMuxPolicy,
) {
    val native = PeerConnection.RTCConfiguration(iceServers.map { it.native }).also {
        it.bundlePolicy = bundlePolicy.asNative()
        it.certificate = certificates?.firstOrNull()?.native
        it.iceCandidatePoolSize = iceCandidatePoolSize
        it.iceTransportsType = iceTransportPolicy.asNative()
        it.rtcpMuxPolicy = rtcpMuxPolicy.asNative()
        it.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
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
