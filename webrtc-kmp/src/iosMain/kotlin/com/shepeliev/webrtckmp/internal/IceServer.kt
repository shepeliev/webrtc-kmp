@file:OptIn(ExperimentalForeignApi::class)

package com.shepeliev.webrtckmp.internal

import WebRTC.RTCIceServer
import WebRTC.RTCTlsCertPolicy
import com.shepeliev.webrtckmp.IceServer
import com.shepeliev.webrtckmp.TlsCertPolicy
import kotlinx.cinterop.ExperimentalForeignApi

internal fun IceServer.toPlatform(): RTCIceServer {
    return RTCIceServer(
        uRLStrings = urls,
        username = username,
        credential = password,
        tlsCertPolicy = tlsCertPolicy.toPlatform(),
        hostname = hostname,
        tlsAlpnProtocols = tlsAlpnProtocols,
        tlsEllipticCurves = tlsEllipticCurves
    )
}

private fun TlsCertPolicy.toPlatform(): RTCTlsCertPolicy {
    return when (this) {
        TlsCertPolicy.TlsCertPolicySecure -> RTCTlsCertPolicy.RTCTlsCertPolicySecure

        TlsCertPolicy.TlsCertPolicyInsecureNoCheck -> {
            RTCTlsCertPolicy.RTCTlsCertPolicyInsecureNoCheck
        }
    }
}
