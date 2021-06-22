package com.shepeliev.webrtckmp

import kotlin.js.Json
import kotlin.js.json

actual class IceServer actual constructor(
    urls: List<String>,
    username: String,
    password: String,
    tlsCertPolicy: TlsCertPolicy,
    hostname: String,
    tlsAlpnProtocols: List<String>?,
    tlsEllipticCurves: List<String>?
) {

    val js: Json

    init {
        js = json(
            "urls" to urls,
            "username" to username,
            "credential" to password
        )
    }

    actual override fun toString(): String = JSON.stringify(js)
}
