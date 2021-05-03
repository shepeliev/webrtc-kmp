package com.shepeliev.webrtckmp

import WebRTC.RTCCryptoOptions

actual class CryptoOptions constructor(val native: RTCCryptoOptions) {
    actual constructor(
        enableGcmCryptoSuites: Boolean,
        enableAes128Sha1_32CryptoCipher: Boolean,
        enableEncryptedRtpHeaderExtensions: Boolean,
        requireFrameEncryption: Boolean
    ) : this(
        RTCCryptoOptions(
            srtpEnableGcmCryptoSuites = enableGcmCryptoSuites,
            srtpEnableAes128Sha1_32CryptoCipher = enableAes128Sha1_32CryptoCipher,
            srtpEnableEncryptedRtpHeaderExtensions =  enableEncryptedRtpHeaderExtensions,
            sframeRequireFrameEncryption = requireFrameEncryption
        )
    )
}
