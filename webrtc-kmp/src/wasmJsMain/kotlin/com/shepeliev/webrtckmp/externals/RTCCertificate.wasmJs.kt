package com.shepeliev.webrtckmp.externals

import com.shepeliev.webrtckmp.KeyType
import kotlinx.coroutines.await
import kotlin.js.Promise

internal actual suspend fun generateRTCCertificate(
    keyType: KeyType,
    expires: Long
): RTCCertificate {
    val options = when (keyType) {
        KeyType.RSA -> createRsaOptions()
        KeyType.ECDSA -> createEcdsaOptions()
    }
    return JsRTCPeerConnection.generateCertificate(options).await()
}

private fun createRsaOptions(): JsAny = js(
    """
    ({
        "name": "RSASSA-PKCS10-v1_5",
        "modulusLength": 2048,
        "publicExponent": new Uint8Array([1, 0, 1]),
        "hash": "SHA-256"
    })
    """
)

private fun createEcdsaOptions(): JsAny = js(
    """
    ({
        "name": "ECDSA",
        "namedCurve": "P-256"
    })
    """
)

@JsName("RTCPeerConnection")
private external class JsRTCPeerConnection {
    companion object {
        fun generateCertificate(options: JsAny): Promise<WasmRTCCertificate>
    }
}

private external interface WasmRTCCertificate : RTCCertificate, JsAny
