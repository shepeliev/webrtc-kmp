package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.RTCBundlePolicy
import dev.onvoid.webrtc.RTCConfiguration
import dev.onvoid.webrtc.RTCIceTransportPolicy
import dev.onvoid.webrtc.RTCRtcpMuxPolicy

actual class RtcConfiguration actual constructor(
    bundlePolicy: BundlePolicy,
    certificates: List<RtcCertificatePem>?,
    iceCandidatePoolSize: Int,
    iceServers: List<IceServer>,
    iceTransportPolicy: IceTransportPolicy,
    rtcpMuxPolicy: RtcpMuxPolicy,
) {
    val jvm = RTCConfiguration().apply {
        this.bundlePolicy = bundlePolicy.asNative()
        this.iceServers = iceServers.map { it.native }
        this.certificates = certificates?.map { it.native }
        this.iceTransportPolicy = iceTransportPolicy.asNative()
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

private fun IceTransportPolicy.asNative(): RTCIceTransportPolicy {
    return when (this) {
        IceTransportPolicy.None -> RTCIceTransportPolicy.NONE
        IceTransportPolicy.Relay -> RTCIceTransportPolicy.RELAY
        IceTransportPolicy.NoHost -> RTCIceTransportPolicy.NO_HOST
        IceTransportPolicy.All -> RTCIceTransportPolicy.ALL
    }
}
