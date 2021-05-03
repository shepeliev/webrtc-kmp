package com.shepeliev.webrtckmp

import org.webrtc.CryptoOptions as NativeCryptoOptions

actual class CryptoOptions constructor(val native: NativeCryptoOptions) {
    actual constructor(
        enableGcmCryptoSuites: Boolean,
        enableAes128Sha1_32CryptoCipher: Boolean,
        enableEncryptedRtpHeaderExtensions: Boolean,
        requireFrameEncryption: Boolean
    ) : this(
        NativeCryptoOptions.builder()
            .setEnableGcmCryptoSuites(enableGcmCryptoSuites)
            .setEnableAes128Sha1_32CryptoCipher(enableAes128Sha1_32CryptoCipher)
            .setEnableEncryptedRtpHeaderExtensions(enableEncryptedRtpHeaderExtensions)
            .setRequireFrameEncryption(requireFrameEncryption)
            .createCryptoOptions()
    )
}
