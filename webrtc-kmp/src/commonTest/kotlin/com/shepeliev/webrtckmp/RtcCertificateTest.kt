package com.shepeliev.webrtckmp

import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class RtcCertificateTest {
    @Test
    fun testGenerateCertificate() = runTest {
        RtcCertificatePem.generateCertificate()
    }
}
