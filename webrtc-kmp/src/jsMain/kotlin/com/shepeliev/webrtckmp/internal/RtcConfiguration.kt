package com.shepeliev.webrtckmp.internal

import com.shepeliev.webrtckmp.RtcConfiguration
import kotlin.js.Json
import kotlin.js.json

internal fun RtcConfiguration.toJs(): Json = json(
    "bundlePolicy" to bundlePolicy.toStringValue(),
    "iceCandidatePoolSize" to iceCandidatePoolSize,
    "iceServers" to iceServers.map { it.toPlatform() }.toTypedArray(),
    "iceTransportPolicy" to iceTransportPolicy.toStringValue(),
    "rtcpMuxPolicy" to rtcpMuxPolicy.toStringValue(),
).apply {
    if (certificates != null) {
        add(json("certificates" to certificates.map { it.js }))
    }
}
