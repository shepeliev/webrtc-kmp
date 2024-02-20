package com.shepeliev.webrtckmp

import kotlin.test.Test
import kotlin.test.assertTrue

class RtcCertificatePemTests {

    @Test
    fun generateEcdsaPem() = runTest {
        val cert = RtcCertificatePem.generateCertificate(KeyType.ECDSA, expires = CERT_EXPIRES_AT_MILLIS)
        assertTrue(cert.certificate.isNotEmpty())
        assertTrue(cert.privateKey.isNotEmpty())
    }

    @Test
    fun generateRsaPem() = runTest {
        val cert = RtcCertificatePem.generateCertificate(KeyType.RSA, expires = CERT_EXPIRES_AT_MILLIS + 6000)
        assertTrue(cert.certificate.isNotEmpty())
        assertTrue(cert.privateKey.isNotEmpty())
    }
}

private const val CERT_EXPIRES_AT_MILLIS = 2655180053000L
