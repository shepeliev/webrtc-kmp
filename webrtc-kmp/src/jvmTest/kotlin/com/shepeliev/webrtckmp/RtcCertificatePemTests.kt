package com.shepeliev.webrtckmp

import org.junit.Test
import kotlin.test.assertTrue

class RtcCertificatePemTests {

    @Test
    fun generateEcdsaPem() = runTest {
        val cert = RtcCertificatePem.generateCertificate(KeyType.ECDSA, expires = System.currentTimeMillis() + 6000)
        assertTrue(cert.certificate.isNotEmpty())
        assertTrue(cert.privateKey.isNotEmpty())
    }

    @Test
    fun generateRsaPem() = runTest {
        val cert = RtcCertificatePem.generateCertificate(KeyType.RSA, expires = System.currentTimeMillis() + 6000)
        assertTrue(cert.certificate.isNotEmpty())
        assertTrue(cert.privateKey.isNotEmpty())
    }
}
