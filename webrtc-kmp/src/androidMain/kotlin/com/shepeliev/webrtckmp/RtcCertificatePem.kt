package com.shepeliev.webrtckmp

import org.webrtc.PeerConnection
import org.webrtc.RtcCertificatePem as NativeRtcCertificatePem

actual class RtcCertificatePem internal constructor(val native: NativeRtcCertificatePem) {
    actual val privateKey: String
        get() = native.privateKey

    actual val certificate: String
        get() = native.certificate

    actual companion object {
        actual suspend fun generateCertificate(keyType: KeyType, expires: Long): RtcCertificatePem {
            return RtcCertificatePem(
                NativeRtcCertificatePem.generateCertificate(keyType.asNative(), expires)
            )
        }
    }
}

private fun KeyType.asNative(): PeerConnection.KeyType {
    return when (this) {
        KeyType.RSA -> PeerConnection.KeyType.RSA
        KeyType.ECDSA -> PeerConnection.KeyType.ECDSA
    }
}
