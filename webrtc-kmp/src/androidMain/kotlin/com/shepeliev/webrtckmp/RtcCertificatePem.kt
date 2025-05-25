package com.shepeliev.webrtckmp

import org.webrtc.PeerConnection
import org.webrtc.RtcCertificatePem as NativeRtcCertificatePem

public actual class RtcCertificatePem internal constructor(
    public val native: NativeRtcCertificatePem,
) {
    public actual val privateKey: String get() = native.privateKey
    public actual val certificate: String get() = native.certificate

    public actual companion object {
        public actual suspend fun generateCertificate(
            keyType: KeyType,
            expires: Long,
        ): RtcCertificatePem =
            RtcCertificatePem(
                NativeRtcCertificatePem.generateCertificate(keyType.toPlatform(), expires),
            )
    }
}

private fun KeyType.toPlatform(): PeerConnection.KeyType =
    when (this) {
        KeyType.RSA -> PeerConnection.KeyType.RSA
        KeyType.ECDSA -> PeerConnection.KeyType.ECDSA
    }
