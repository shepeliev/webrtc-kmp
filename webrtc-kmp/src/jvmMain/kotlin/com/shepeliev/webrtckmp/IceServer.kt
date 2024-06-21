package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.RTCIceServer
import dev.onvoid.webrtc.TlsCertPolicy as NativeTlsCertPolicy

fun IceServer.asNative(): RTCIceServer = RTCIceServer().apply {
    urls = this@asNative.urls
    username = this@asNative.username
    password = this@asNative.password
    tlsCertPolicy = this@asNative.tlsCertPolicy.asNative()
    hostname = this@asNative.hostname
    tlsAlpnProtocols = this@asNative.tlsAlpnProtocols
    tlsEllipticCurves = this@asNative.tlsEllipticCurves
}

private fun TlsCertPolicy.asNative(): NativeTlsCertPolicy {
    return when (this) {
        TlsCertPolicy.TlsCertPolicySecure -> NativeTlsCertPolicy.SECURE

        TlsCertPolicy.TlsCertPolicyInsecureNoCheck -> {
            NativeTlsCertPolicy.INSECURE_NO_CHECK
        }
    }
}
