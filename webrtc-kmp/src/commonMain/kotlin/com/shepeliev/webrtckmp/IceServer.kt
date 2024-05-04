package com.shepeliev.webrtckmp

data class IceServer(
    val urls: List<String>,
    val username: String = "",
    val password: String = "",
    val tlsCertPolicy: TlsCertPolicy = TlsCertPolicy.TlsCertPolicySecure,
    val hostname: String = "",
    val tlsAlpnProtocols: List<String>? = null,
    val tlsEllipticCurves: List<String>? = null
)
