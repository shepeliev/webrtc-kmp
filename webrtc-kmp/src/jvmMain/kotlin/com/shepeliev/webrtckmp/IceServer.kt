package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.RTCIceServer as NativeIceServer
import dev.onvoid.webrtc.TlsCertPolicy as NativeTlsCertPolicy

actual class IceServer internal constructor(val native: NativeIceServer) {
    actual constructor(
        urls: List<String>,
        username: String,
        password: String,
        tlsCertPolicy: TlsCertPolicy,
        hostname: String,
        tlsAlpnProtocols: List<String>?,
        tlsEllipticCurves: List<String>?
    ) : this(
        NativeIceServer().apply {
            this.urls = urls
            this.username = username
            this.password = password
            this.tlsCertPolicy = tlsCertPolicy.asNative()
            this.hostname = hostname
            this.tlsAlpnProtocols = tlsAlpnProtocols
            this.tlsEllipticCurves = tlsEllipticCurves
        }
    )

    actual override fun toString(): String = native.toString()
}

private fun TlsCertPolicy.asNative(): NativeTlsCertPolicy {
    return when (this) {
        TlsCertPolicy.TlsCertPolicySecure -> NativeTlsCertPolicy.SECURE

        TlsCertPolicy.TlsCertPolicyInsecureNoCheck -> {
            NativeTlsCertPolicy.INSECURE_NO_CHECK
        }
    }
}
