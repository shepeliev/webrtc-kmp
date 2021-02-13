package com.shepeliev.webrtckmm

import kotlin.test.Test
import kotlin.test.assertEquals

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
