package com.shepeliev.webrtckmp

public expect class RtcCertificatePem {
    @Deprecated("Will be removed in order to comply with JS/WASM")
    public val privateKey: String

    @Deprecated("Will be removed in order to comply with JS/WASM")
    public val certificate: String

    public companion object {
        public suspend fun generateCertificate(
            keyType: KeyType = KeyType.ECDSA,
            expires: Long = 2592000L,
        ): RtcCertificatePem
    }
}
