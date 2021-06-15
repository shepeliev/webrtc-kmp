package com.shepeliev.webrtckmp

import kotlinx.coroutines.await
import org.khronos.webgl.Uint8Array
import kotlin.js.json

actual class RtcCertificatePem internal constructor(val js: RTCCertificate) {
    actual val privateKey: String
        get() = ""

    actual val certificate: String
        get() = ""

    actual companion object {
        actual suspend fun generateCertificate(keyType: KeyType, expires: Long): RtcCertificatePem {
            val options = when (keyType) {
                KeyType.RSA ->                     json(
                        "name" to "RSASSA-PKCS10-v1_5",
                        "modulusLength" to 2048,
                        "publicExponent" to Uint8Array(arrayOf(1, 0, 1)),
                        "hash" to "SHA-256",
                    )
                KeyType.ECDSA -> json(
                    "name" to "ECDSA",
                    "namedCurve" to "P-256",
                )
            }
            return RtcCertificatePem(RTCPeerConnection.generateCertificate(options).await())
        }
    }
}
