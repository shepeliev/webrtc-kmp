package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.RTCBundlePolicy
import dev.onvoid.webrtc.RTCConfiguration
import dev.onvoid.webrtc.RTCIceTransportPolicy
import dev.onvoid.webrtc.RTCRtcpMuxPolicy

internal fun RtcConfiguration.asNative(): RTCConfiguration = RTCConfiguration().apply {
    bundlePolicy = this@asNative.bundlePolicy.asNative()
    certificates = this@asNative.certificates?.map { it.native }
    iceServers = this@asNative.iceServers.map { it.asNative() }
    iceTransportPolicy = this@asNative.iceTransportPolicy.asNative()
    rtcpMuxPolicy = this@asNative.rtcpMuxPolicy.asNative()
}

internal fun RtcpMuxPolicy.asNative(): RTCRtcpMuxPolicy = when (this) {
    RtcpMuxPolicy.Negotiate -> RTCRtcpMuxPolicy.NEGOTIATE
    RtcpMuxPolicy.Require -> RTCRtcpMuxPolicy.REQUIRE
}

internal fun BundlePolicy.asNative(): RTCBundlePolicy = when (this) {
    BundlePolicy.Balanced -> RTCBundlePolicy.BALANCED
    BundlePolicy.MaxBundle -> RTCBundlePolicy.MAX_BUNDLE
    BundlePolicy.MaxCompat -> RTCBundlePolicy.MAX_COMPAT
}

internal fun IceTransportPolicy.asNative(): RTCIceTransportPolicy = when(this) {
    IceTransportPolicy.None -> RTCIceTransportPolicy.NONE
    IceTransportPolicy.Relay -> RTCIceTransportPolicy.RELAY
    IceTransportPolicy.NoHost -> RTCIceTransportPolicy.NO_HOST
    IceTransportPolicy.All -> RTCIceTransportPolicy.ALL
}
