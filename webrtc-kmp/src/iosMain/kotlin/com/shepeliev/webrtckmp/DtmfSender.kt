@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.shepeliev.webrtckmp

import WebRTC.RTCDtmfSenderProtocol

actual class DtmfSender(val native: RTCDtmfSenderProtocol) {

    actual val canInsertDtmf: Boolean
        get() = native.canInsertDtmf()

    actual val duration: Int
        get() = (native.duration() * 1000).toInt()

    actual val interToneGap: Int
        get() = (native.interToneGap() * 1000).toInt()

    actual fun insertDtmf(tones: String, durationMs: Int, interToneGapMs: Int): Boolean {
        return native.insertDtmf(tones, durationMs / 1000.0, interToneGapMs / 1000.0)
    }

    actual fun tones(): String = native.remainingTones()
}
