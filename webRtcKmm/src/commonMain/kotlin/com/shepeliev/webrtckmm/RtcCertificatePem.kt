package com.shepeliev.webrtckmm

private const val DEFAULT_EXPIRE = 2592000L

expect class RtcCertificatePem {
    val privateKey: String
    val certificate: String

    companion object {
        fun generateCertificate(
            keyType: KeyType = KeyType.ECDSA,
            expires: Long = DEFAULT_EXPIRE
        ): RtcCertificatePem
    }
}
