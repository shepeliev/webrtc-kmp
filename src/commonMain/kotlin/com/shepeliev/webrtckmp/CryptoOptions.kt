package com.shepeliev.webrtckmp

expect class CryptoOptions(
    enableGcmCryptoSuites: Boolean = false,
    enableAes128Sha1_32CryptoCipher: Boolean = false,
    enableEncryptedRtpHeaderExtensions: Boolean = false,
    requireFrameEncryption: Boolean = false,
)
