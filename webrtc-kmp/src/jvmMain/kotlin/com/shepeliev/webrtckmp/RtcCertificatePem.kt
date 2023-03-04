package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.RTCCertificatePEM

actual class RtcCertificatePem(val native: RTCCertificatePEM) {
    actual val privateKey: String
        get() = native.privateKey

    actual val certificate: String
        get() = native.certificate

    val expires: Long
        get() = native.expires

    actual companion object {
        actual suspend fun generateCertificate(
            keyType: KeyType,
            expires: Long
        ): RtcCertificatePem {
            TODO("Not yet implemented!")
        }
    }
}
