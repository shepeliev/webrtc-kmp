package com.shepeliev.webrtckmp

import com.shepeliev.webrtckmp.externals.RTCCertificate
import com.shepeliev.webrtckmp.externals.generateRTCCertificate

public actual class RtcCertificatePem internal constructor(
    internal val js: RTCCertificate,
) {
    public actual val privateKey: String get() = ""
    public actual val certificate: String get() = ""

    public actual companion object {
        public actual suspend fun generateCertificate(
            keyType: KeyType,
            expires: Long,
        ): RtcCertificatePem = RtcCertificatePem(generateRTCCertificate(keyType, expires))
    }
}
