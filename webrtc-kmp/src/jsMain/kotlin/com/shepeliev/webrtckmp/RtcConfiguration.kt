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

    actual val bundlePolicy: BundlePolicy = js["bundlePolicy"].toString().toBundlePolicy()
    actual val certificates: List<RtcCertificatePem>? = js["certificates"]?.let { certs ->
        certs.unsafeCast<Array<RTCCertificate>>().map { RtcCertificatePem(it) }
    }
    actual val iceCandidatePoolSize: Int = js["iceCandidatePoolSize"].unsafeCast<Int>()
    actual val iceServers: List<IceServer> = js["iceServers"].unsafeCast<Array<Json>>().map {
        IceServer(
            urls = it["urls"].unsafeCast<Array<String>>().toList(),
            username = it["username"].unsafeCast<String?>() ?: "",
            password = it["credential"].unsafeCast<String?>() ?: "",
        )
    }
    actual val iceTransportPolicy: IceTransportPolicy = js["iceTransportPolicy"].toString().toIceTransportPolicy()
    actual val rtcpMuxPolicy: RtcpMuxPolicy = js["rtcpMuxPolicy"].toString().toRtcpMuxPolicy()
    actual val continualGatheringPolicy: ContinualGatheringPolicy = ContinualGatheringPolicy.GatherOnce
}

private fun String.toBundlePolicy(): BundlePolicy = when (this) {
    "balanced" -> BundlePolicy.Balanced
    "max-bundle" -> BundlePolicy.MaxBundle
    "max-compat" -> BundlePolicy.MaxCompat
    else -> BundlePolicy.Balanced
}

private fun String.toIceTransportPolicy(): IceTransportPolicy = when (this) {
    "all" -> IceTransportPolicy.All
    "relay" -> IceTransportPolicy.Relay
    else -> IceTransportPolicy.All
}

private fun String.toRtcpMuxPolicy(): RtcpMuxPolicy = when (this) {
    "negotiate" -> RtcpMuxPolicy.Negotiate
    "require" -> RtcpMuxPolicy.Require
    else -> RtcpMuxPolicy.Negotiate
}
