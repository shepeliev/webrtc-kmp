package com.shepeliev.webrtckmp

import org.webrtc.PeerConnection

internal fun RtcConfiguration.toPlatform() = PeerConnection.RTCConfiguration(iceServers.map { it.toPlatform() })
    .also {
        it.bundlePolicy = bundlePolicy.toPlatform()
        it.certificate = certificates?.firstOrNull()?.native
        it.iceCandidatePoolSize = iceCandidatePoolSize
        it.iceTransportsType = iceTransportPolicy.toPlatform()
        it.rtcpMuxPolicy = rtcpMuxPolicy.toPlatform()
        it.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        it.continualGatheringPolicy = continualGatheringPolicy.toPlatform()
    }


private fun RtcpMuxPolicy.toPlatform(): PeerConnection.RtcpMuxPolicy {
    return when (this) {
        RtcpMuxPolicy.Negotiate -> PeerConnection.RtcpMuxPolicy.NEGOTIATE
        RtcpMuxPolicy.Require -> PeerConnection.RtcpMuxPolicy.REQUIRE
    }
}

private fun BundlePolicy.toPlatform(): PeerConnection.BundlePolicy {
    return when (this) {
        BundlePolicy.Balanced -> PeerConnection.BundlePolicy.BALANCED
        BundlePolicy.MaxBundle -> PeerConnection.BundlePolicy.MAXBUNDLE
        BundlePolicy.MaxCompat -> PeerConnection.BundlePolicy.MAXCOMPAT
    }
}

private fun IceTransportPolicy.toPlatform(): PeerConnection.IceTransportsType {
    return when (this) {
        IceTransportPolicy.None -> PeerConnection.IceTransportsType.NONE
        IceTransportPolicy.Relay -> PeerConnection.IceTransportsType.RELAY
        IceTransportPolicy.NoHost -> PeerConnection.IceTransportsType.NOHOST
        IceTransportPolicy.All -> PeerConnection.IceTransportsType.ALL
    }
}

private fun ContinualGatheringPolicy.toPlatform(): PeerConnection.ContinualGatheringPolicy {
    return when (this) {
        ContinualGatheringPolicy.GatherOnce -> PeerConnection.ContinualGatheringPolicy.GATHER_ONCE
        ContinualGatheringPolicy.GatherContinually -> PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
    }
}
