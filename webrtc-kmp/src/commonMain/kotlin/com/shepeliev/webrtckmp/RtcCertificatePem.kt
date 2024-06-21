package com.shepeliev.webrtckmp

expect class RtcCertificatePem {
    @Deprecated("Will be removed in order to comply with JS/WASM")
    val privateKey: String
    @Deprecated("Will be removed in order to comply with JS/WASM")
    val certificate: String

    companion object {
        suspend fun generateCertificate(
            keyType: KeyType = KeyType.ECDSA,
            expires: Long = 2592000L
        ): RtcCertificatePem
    }
}
