package com.shepeliev.webrtckmp

import WebRTC.RTCBundlePolicy
import WebRTC.RTCConfiguration
import WebRTC.RTCContinualGatheringPolicy
import WebRTC.RTCIceTransportPolicy
import WebRTC.RTCRtcpMuxPolicy
import WebRTC.RTCSdpSemantics

actual class RtcConfiguration actual constructor(
    bundlePolicy: BundlePolicy,
    certificates: List<RtcCertificatePem>?,
    iceCandidatePoolSize: Int,
    iceServers: List<IceServer>,
    iceTransportPolicy: IceTransportPolicy,
    rtcpMuxPolicy: RtcpMuxPolicy,
    continualGatheringPolicy: ContinualGatheringPolicy,
) {
    val native: RTCConfiguration = RTCConfiguration().apply {
        this.bundlePolicy = bundlePolicy.asNative()
        this.certificate = certificates?.firstOrNull()?.native
        this.iceCandidatePoolSize = iceCandidatePoolSize
        this.iceServers = iceServers.map(IceServer::native)
        this.rtcpMuxPolicy = rtcpMuxPolicy.asNative()
        this.iceTransportPolicy = iceTransportPolicy.asNative()
        this.sdpSemantics = RTCSdpSemantics.RTCSdpSemanticsUnifiedPlan
        this.continualGatheringPolicy = continualGatheringPolicy.asNative()
    }
}

private fun RtcpMuxPolicy.asNative(): RTCRtcpMuxPolicy {
    return when (this) {
        RtcpMuxPolicy.Negotiate -> RTCRtcpMuxPolicy.RTCRtcpMuxPolicyNegotiate
        RtcpMuxPolicy.Require -> RTCRtcpMuxPolicy.RTCRtcpMuxPolicyRequire
    }
}

private fun BundlePolicy.asNative(): RTCBundlePolicy {
    return when (this) {
        BundlePolicy.Balanced -> RTCBundlePolicy.RTCBundlePolicyBalanced
        BundlePolicy.MaxBundle -> RTCBundlePolicy.RTCBundlePolicyMaxBundle
        BundlePolicy.MaxCompat -> RTCBundlePolicy.RTCBundlePolicyMaxCompat
    }
}

private fun IceTransportPolicy.asNative(): RTCIceTransportPolicy {
    return when (this) {
        IceTransportPolicy.None -> RTCIceTransportPolicy.RTCIceTransportPolicyNone
        IceTransportPolicy.Relay -> RTCIceTransportPolicy.RTCIceTransportPolicyRelay
        IceTransportPolicy.NoHost -> RTCIceTransportPolicy.RTCIceTransportPolicyNoHost
        IceTransportPolicy.All -> RTCIceTransportPolicy.RTCIceTransportPolicyAll
    }
}

private fun ContinualGatheringPolicy.asNative(): RTCContinualGatheringPolicy {
    return when (this) {
        ContinualGatheringPolicy.GatherOnce -> RTCContinualGatheringPolicy.RTCContinualGatheringPolicyGatherOnce
        ContinualGatheringPolicy.GatherContinually -> RTCContinualGatheringPolicy.RTCContinualGatheringPolicyGatherContinually
    }
}
