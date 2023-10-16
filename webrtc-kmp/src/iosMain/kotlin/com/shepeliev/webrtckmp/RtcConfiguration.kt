package com.shepeliev.webrtckmp

import WebRTC.RTCBundlePolicy
import WebRTC.RTCConfiguration
import WebRTC.RTCContinualGatheringPolicy
import WebRTC.RTCIceServer
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
    actual val bundlePolicy: BundlePolicy = native.bundlePolicy.asCommon()
    actual val certificates: List<RtcCertificatePem>? = native.certificate?.let { listOf(RtcCertificatePem(it)) }
    actual val iceCandidatePoolSize: Int = native.iceCandidatePoolSize
    actual val iceServers: List<IceServer> = native.iceServers.map { IceServer(it as RTCIceServer) }
    actual val iceTransportPolicy: IceTransportPolicy = native.iceTransportPolicy.asCommon()
    actual val rtcpMuxPolicy: RtcpMuxPolicy = native.rtcpMuxPolicy.asCommon()
    actual val continualGatheringPolicy: ContinualGatheringPolicy = native.continualGatheringPolicy.asCommon()
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

private fun RTCRtcpMuxPolicy.asCommon(): RtcpMuxPolicy {
    return when (this) {
        RTCRtcpMuxPolicy.RTCRtcpMuxPolicyNegotiate -> RtcpMuxPolicy.Negotiate
        RTCRtcpMuxPolicy.RTCRtcpMuxPolicyRequire -> RtcpMuxPolicy.Require
        else -> error("Unknown RTCRtcpMuxPolicy: $this")
    }
}

private fun RTCBundlePolicy.asCommon(): BundlePolicy {
    return when (this) {
        RTCBundlePolicy.RTCBundlePolicyBalanced -> BundlePolicy.Balanced
        RTCBundlePolicy.RTCBundlePolicyMaxBundle -> BundlePolicy.MaxBundle
        RTCBundlePolicy.RTCBundlePolicyMaxCompat -> BundlePolicy.MaxCompat
        else -> error("Unknown RTCBundlePolicy: $this")
    }
}

private fun RTCIceTransportPolicy.asCommon(): IceTransportPolicy {
    return when (this) {
        RTCIceTransportPolicy.RTCIceTransportPolicyNone -> IceTransportPolicy.None
        RTCIceTransportPolicy.RTCIceTransportPolicyRelay -> IceTransportPolicy.Relay
        RTCIceTransportPolicy.RTCIceTransportPolicyNoHost -> IceTransportPolicy.NoHost
        RTCIceTransportPolicy.RTCIceTransportPolicyAll -> IceTransportPolicy.All
        else -> error("Unknown RTCIceTransportPolicy: $this")
    }
}

private fun RTCContinualGatheringPolicy.asCommon(): ContinualGatheringPolicy {
    return when (this) {
        RTCContinualGatheringPolicy.RTCContinualGatheringPolicyGatherOnce -> ContinualGatheringPolicy.GatherOnce
        RTCContinualGatheringPolicy.RTCContinualGatheringPolicyGatherContinually -> ContinualGatheringPolicy.GatherContinually
        else -> error("Unknown RTCContinualGatheringPolicy: $this")
    }
}
