package com.shepeliev.webrtckmm

import kotlin.jvm.JvmOverloads

expect class IceServer @JvmOverloads constructor(
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
