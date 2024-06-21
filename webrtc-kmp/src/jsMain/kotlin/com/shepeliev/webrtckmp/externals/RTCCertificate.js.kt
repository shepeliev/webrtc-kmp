package com.shepeliev.webrtckmp.externals

import com.shepeliev.webrtckmp.KeyType
import kotlinx.coroutines.await
import org.khronos.webgl.Uint8Array
import kotlin.js.Promise
import kotlin.js.json

internal actual suspend fun generateRTCCertificate(
    keyType: KeyType,
    expires: Long
): RTCCertificate {
    val options = when (keyType) {
        KeyType.RSA -> json(
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

    return JsRTCPeerConnection.generateCertificate(options).await()
}
