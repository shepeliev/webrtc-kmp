package com.shepeliev.webrtckmp

expect class RtcCertificatePem {
    val privateKey: String
    val certificate: String

    companion object {
        suspend fun generateCertificate(
            keyType: KeyType = KeyType.ECDSA,
            expires: Long = 2592000L
        ): RtcCertificatePem
    }
}
