package com.shepeliev.webrtckmp

import com.shepeliev.webrtckmp.externals.RTCCertificate
import com.shepeliev.webrtckmp.externals.generateRTCCertificate

actual class RtcCertificatePem internal constructor(val js: RTCCertificate) {
    actual val privateKey: String
        get() = ""

    actual val certificate: String
        get() = ""

    actual companion object {
        actual suspend fun generateCertificate(keyType: KeyType, expires: Long): RtcCertificatePem {
            return RtcCertificatePem(generateRTCCertificate(keyType, expires))
        }
    }
}
