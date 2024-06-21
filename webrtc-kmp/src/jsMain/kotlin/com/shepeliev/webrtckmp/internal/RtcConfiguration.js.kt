package com.shepeliev.webrtckmp.internal

import com.shepeliev.webrtckmp.RtcConfiguration
import com.shepeliev.webrtckmp.externals.RTCPeerConnectionConfiguration
import kotlin.js.json

internal actual fun RtcConfiguration.toPlatform(): RTCPeerConnectionConfiguration {
    val js = json(
        "bundlePolicy" to bundlePolicy.toStringValue(),
        "iceCandidatePoolSize" to iceCandidatePoolSize,
        "iceServers" to iceServers.map { it.toPlatform() }.toTypedArray(),
        "iceTransportPolicy" to iceTransportPolicy.toStringValue(),
        "rtcpMuxPolicy" to rtcpMuxPolicy.toStringValue(),
    )

    if (certificates != null) {
        js.add(json("certificates" to certificates.map { it.js }))
    }

    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    return js.asDynamic() as RTCPeerConnectionConfiguration
}
