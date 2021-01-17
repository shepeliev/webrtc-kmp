package com.shepeliev.webrtckmm

import kotlin.jvm.JvmOverloads

data class CryptoOptions @JvmOverloads constructor(
    var enableGcmCryptoSuites: Boolean = false,
    val enableAes128Sha1_32CryptoCipher: Boolean = false,
    val enableEncryptedRtpHeaderExtensions: Boolean = false,
    val requireFrameEncryption: Boolean = false,
)
