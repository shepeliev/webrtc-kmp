package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.RTCBundlePolicy
import dev.onvoid.webrtc.RTCConfiguration
import dev.onvoid.webrtc.RTCRtcpMuxPolicy

actual class RtcConfiguration actual constructor(
    bundlePolicy: BundlePolicy,
    certificates: List<RtcCertificatePem>?,
    iceCandidatePoolSize: Int,
    iceServers: List<IceServer>,
    iceTransportPolicy: IceTransportPolicy,
    rtcpMuxPolicy: RtcpMuxPolicy,
) {
    val native = RTCConfiguration().apply {
        this.iceServers = iceServers.map { it.native }
        this.bundlePolicy = bundlePolicy.asNative()
        this.certificates = certificates?.map { it.native }
        this.rtcpMuxPolicy = rtcpMuxPolicy.asNative()
    }
}

private fun RtcpMuxPolicy.asNative(): RTCRtcpMuxPolicy {
    return when (this) {
        RtcpMuxPolicy.Negotiate -> RTCRtcpMuxPolicy.NEGOTIATE
        RtcpMuxPolicy.Require -> RTCRtcpMuxPolicy.REQUIRE
    }
}

private fun BundlePolicy.asNative(): RTCBundlePolicy {
    return when (this) {
        BundlePolicy.Balanced -> RTCBundlePolicy.BALANCED
        BundlePolicy.MaxBundle -> RTCBundlePolicy.MAX_BUNDLE
        BundlePolicy.MaxCompat -> RTCBundlePolicy.MAX_COMPAT
    }
}
