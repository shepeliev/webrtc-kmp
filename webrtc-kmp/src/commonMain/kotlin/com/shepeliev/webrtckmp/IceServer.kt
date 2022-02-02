package com.shepeliev.webrtckmp

expect class IceServer(
    urls: List<String>,
    username: String = "",
    password: String = "",
    tlsCertPolicy: TlsCertPolicy = TlsCertPolicy.TlsCertPolicySecure,
    hostname: String = "",
    tlsAlpnProtocols: List<String>? = null,
    tlsEllipticCurves: List<String>? = null
) {
    override fun toString(): String
}
