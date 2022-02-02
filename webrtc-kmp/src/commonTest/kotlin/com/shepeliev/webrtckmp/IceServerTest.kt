package com.shepeliev.webrtckmp

import kotlin.test.Test

class IceServerTest {

    @Test
    fun should_build_successfully() {
        IceServer(
            urls = listOf("stun:url.to.stun.com"),
            username = "username",
            password = "password",
            tlsCertPolicy = TlsCertPolicy.TlsCertPolicySecure,
        )
    }
}
