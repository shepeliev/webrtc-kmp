package com.shepeliev.webrtckmp

import WebRTC.RTCIceServer

actual class IceServer internal constructor(val native: RTCIceServer) {
    actual constructor(
        urls: List<String>,
        username: String,
        password: String,
        tlsCertPolicy: TlsCertPolicy,
        hostname: String,
        tlsAlpnProtocols: List<String>?,
        tlsEllipticCurves: List<String>?
    ): this(
        RTCIceServer(
            uRLStrings = urls,
            username = username,
            credential = password,
            tlsCertPolicy = tlsCertPolicy.asNative(),
            hostname = hostname,
            tlsAlpnProtocols = tlsAlpnProtocols,
            tlsEllipticCurves = tlsEllipticCurves
        )
    )

    actual override fun toString(): String = native.toString()
}
