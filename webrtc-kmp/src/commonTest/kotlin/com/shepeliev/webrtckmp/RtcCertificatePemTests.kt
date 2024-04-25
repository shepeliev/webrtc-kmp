package com.shepeliev.webrtckmp

import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue

class RtcCertificatePemTests {

    @BeforeTest
    fun setup() {
        setupMocks()
    }

    @Test
    @Ignore // TODO fails CI JS tests
    fun generateEcdsaPem() = runTest {
        val cert = RtcCertificatePem.generateCertificate(KeyType.ECDSA)
        assertTrue(cert.certificate.isNotEmpty())
        assertTrue(cert.privateKey.isNotEmpty())
    }

    @Test
    @Ignore // TODO fails CI JS tests
    fun generateRsaPem() = runTest {
        val cert = RtcCertificatePem.generateCertificate(KeyType.RSA)
        assertTrue(cert.certificate.isNotEmpty())
        assertTrue(cert.privateKey.isNotEmpty())
    }
}
