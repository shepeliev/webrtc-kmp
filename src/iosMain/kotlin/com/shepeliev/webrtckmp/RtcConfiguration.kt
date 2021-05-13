package com.shepeliev.webrtckmp

import WebRTC.RTCBundlePolicy
import WebRTC.RTCConfiguration
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
) {
    val native: RTCConfiguration = RTCConfiguration().also {
        it.bundlePolicy = bundlePolicy.asNative()
        it.certificate = certificates?.firstOrNull()?.native
        it.iceCandidatePoolSize = iceCandidatePoolSize
        it.iceServers = iceServers.map(IceServer::native)
        it.rtcpMuxPolicy = rtcpMuxPolicy.asNative()
        it.iceTransportPolicy = iceTransportPolicy.asNative()
        it.sdpSemantics = RTCSdpSemantics.RTCSdpSemanticsUnifiedPlan
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
    return when(this) {
        IceTransportPolicy.None -> RTCIceTransportPolicy.RTCIceTransportPolicyNone
        IceTransportPolicy.Relay -> RTCIceTransportPolicy.RTCIceTransportPolicyRelay
        IceTransportPolicy.NoHost -> RTCIceTransportPolicy.RTCIceTransportPolicyNoHost
        IceTransportPolicy.All -> RTCIceTransportPolicy.RTCIceTransportPolicyAll
    }
}
