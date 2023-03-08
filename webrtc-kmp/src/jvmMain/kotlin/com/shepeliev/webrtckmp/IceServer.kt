package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.RTCIceServer

actual class IceServer internal constructor(val native: RTCIceServer) {
    actual constructor(
        urls: List<String>,
        username: String,
        password: String,
        tlsCertPolicy: TlsCertPolicy,
        hostname: String,
        tlsAlpnProtocols: List<String>?,
        tlsEllipticCurves: List<String>?,
    ) : this(
        RTCIceServer().apply {
            this.urls = urls
            this.username = username
            this.password = password
            this.tlsCertPolicy = tlsCertPolicy.asNative()
            this.hostname = hostname
            this.tlsAlpnProtocols = tlsAlpnProtocols
            this.tlsEllipticCurves = tlsEllipticCurves
        },
    )

    actual override fun toString(): String = native.toString()
}

private fun TlsCertPolicy.asNative(): dev.onvoid.webrtc.TlsCertPolicy {
    return when (this) {
        TlsCertPolicy.TlsCertPolicySecure -> dev.onvoid.webrtc.TlsCertPolicy.SECURE
        TlsCertPolicy.TlsCertPolicyInsecureNoCheck -> dev.onvoid.webrtc.TlsCertPolicy.INSECURE_NO_CHECK
    }
}
