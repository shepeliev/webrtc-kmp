package com.shepeliev.webrtckmp.externals

import com.shepeliev.webrtckmp.KeyType

external interface RTCCertificate {
    val expires: Date
}

internal expect suspend fun generateRTCCertificate(keyType: KeyType, expires: Long): RTCCertificate
