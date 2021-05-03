package com.shepeliev.webrtckmp

import WebRTC.RTCCertificate

actual class RtcCertificatePem(val native: RTCCertificate) {
    actual val privateKey: String
        get() = native.private_key

    actual val certificate: String
        get() = native.certificate

    actual companion object {
        actual fun generateCertificate(keyType: KeyType, expires: Long): RtcCertificatePem {
            val keyTypeValue = when (keyType) {
                KeyType.ECDSA -> "ECDSA"
                KeyType.RSA -> "RSASSA-PKCS1-v1_5"
            }
            val params: Map<Any?, Comparable<*>> = mapOf(
                "expires" to expires,
                "name" to keyTypeValue
            )
            return RtcCertificatePem(RTCCertificate.generateCertificateWithParams(params)!!)
        }
    }
}
