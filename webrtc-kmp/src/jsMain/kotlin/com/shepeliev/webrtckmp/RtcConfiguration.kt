package com.shepeliev.webrtckmp

import kotlin.js.Json
import kotlin.js.json

actual class RtcConfiguration actual constructor(
    bundlePolicy: BundlePolicy,
    certificates: List<RtcCertificatePem>?,
    iceCandidatePoolSize: Int,
    iceServers: List<IceServer>,
    iceTransportPolicy: IceTransportPolicy,
    rtcpMuxPolicy: RtcpMuxPolicy,
    continualGatheringPolicy: ContinualGatheringPolicy,
) {
    val js: Json

    init {
        js = json(
            "bundlePolicy" to bundlePolicy.toJs(),
            "iceCandidatePoolSize" to iceCandidatePoolSize,
            "iceServers" to iceServers.map { it.js }.toTypedArray(),
            "iceTransportPolicy" to iceTransportPolicy.toJs(),
            "rtcpMuxPolicy" to rtcpMuxPolicy.toJs(),
        )

        if (certificates != null) {
            js.add(json("certificates" to certificates.map { it.js }))
        }
    }

    private fun BundlePolicy.toJs(): String = when (this) {
        BundlePolicy.Balanced -> "balanced"
        BundlePolicy.MaxBundle -> "max-bundle"
        BundlePolicy.MaxCompat -> "max-compat"
    }

    private fun IceTransportPolicy.toJs(): String = when (this) {
        IceTransportPolicy.All -> "all"
        IceTransportPolicy.Relay -> "relay"
        else -> "all"
    }

    private fun RtcpMuxPolicy.toJs(): String = when (this) {
        RtcpMuxPolicy.Negotiate -> "negotiate"
        RtcpMuxPolicy.Require -> "require"
    }
}
