package com.shepeliev.webrtckmm

expect class CryptoOptions(
    enableGcmCryptoSuites: Boolean = false,
    enableAes128Sha1_32CryptoCipher: Boolean = false,
    enableEncryptedRtpHeaderExtensions: Boolean = false,
    requireFrameEncryption: Boolean = false,
)
