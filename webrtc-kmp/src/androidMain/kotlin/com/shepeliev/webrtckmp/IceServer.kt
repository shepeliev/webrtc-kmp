package com.shepeliev.webrtckmp

import org.webrtc.PeerConnection

internal fun IceServer.toPlatform() = PeerConnection.IceServer.builder(urls)
    .setUsername(username)
    .setPassword(password)
    .setTlsCertPolicy(tlsCertPolicy.toPlatform())
    .setHostname(hostname)
    .setTlsAlpnProtocols(tlsAlpnProtocols)
    .setTlsEllipticCurves(tlsEllipticCurves)
    .createIceServer()

private fun TlsCertPolicy.toPlatform(): PeerConnection.TlsCertPolicy {
    return when (this) {
        TlsCertPolicy.TlsCertPolicySecure -> PeerConnection.TlsCertPolicy.TLS_CERT_POLICY_SECURE

        TlsCertPolicy.TlsCertPolicyInsecureNoCheck -> {
            PeerConnection.TlsCertPolicy.TLS_CERT_POLICY_INSECURE_NO_CHECK
        }
    }
}
