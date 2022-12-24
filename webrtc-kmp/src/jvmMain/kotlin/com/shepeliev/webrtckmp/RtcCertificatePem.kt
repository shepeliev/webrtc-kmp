package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.RTCCertificatePEM

actual class RtcCertificatePem internal constructor(val native: RTCCertificatePEM) {
    actual val privateKey: String
        get() = native.privateKey

    actual val certificate: String
        get() = native.certificate

    actual companion object {
        actual suspend fun generateCertificate(keyType: KeyType, expires: Long): RtcCertificatePem {
            RTCCertificatePEM(keyType.asNative(), certificate, expires)
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
