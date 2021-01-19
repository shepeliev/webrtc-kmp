package com.shepeliev.webrtckmm

import org.webrtc.RtcCertificatePem as NativeRtcCertificatePem

actual class RtcCertificatePem(val native: NativeRtcCertificatePem) {
    actual val privateKey: String
        get() = native.privateKey

    actual val certificate: String
        get() = native.certificate

    actual companion object {
        actual fun generateCertificate(keyType: KeyType, expires: Long): RtcCertificatePem {
            return RtcCertificatePem(
                NativeRtcCertificatePem.generateCertificate(
                    keyType.toNative(),
                    expires
                )
            )
        }
    }
}

internal fun NativeRtcCertificatePem.toCommon() = RtcCertificatePem(this)
