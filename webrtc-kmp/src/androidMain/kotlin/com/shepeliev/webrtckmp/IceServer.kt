package com.shepeliev.webrtckmp

import org.webrtc.PeerConnection

actual class IceServer internal constructor(val native: PeerConnection.IceServer) {
    actual constructor(
        urls: List<String>,
        username: String,
        password: String,
        tlsCertPolicy: TlsCertPolicy,
        hostname: String,
        tlsAlpnProtocols: List<String>?,
        tlsEllipticCurves: List<String>?
    ) : this(
        PeerConnection.IceServer.builder(urls)
            .setUsername(username)
            .setPassword(password)
            .setTlsCertPolicy(tlsCertPolicy.asNative())
            .setHostname(hostname)
            .setTlsAlpnProtocols(tlsAlpnProtocols)
            .setTlsEllipticCurves(tlsEllipticCurves)
            .createIceServer()
    )

    actual override fun toString(): String = native.toString()
}

private fun TlsCertPolicy.asNative(): PeerConnection.TlsCertPolicy {
    return when (this) {
        TlsCertPolicy.TlsCertPolicySecure -> PeerConnection.TlsCertPolicy.TLS_CERT_POLICY_SECURE

        TlsCertPolicy.TlsCertPolicyInsecureNoCheck -> {
            PeerConnection.TlsCertPolicy.TLS_CERT_POLICY_INSECURE_NO_CHECK
        }
    }
}
