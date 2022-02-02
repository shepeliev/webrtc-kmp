package com.shepeliev.webrtckmp

import org.webrtc.DtmfSender as NativeDtmfSender

actual class DtmfSender(val native: NativeDtmfSender) {

    actual val canInsertDtmf: Boolean
        get() = native.canInsertDtmf()

    actual val duration: Int
        get() = native.duration()

    actual val interToneGap: Int
        get() = native.interToneGap()

    actual fun insertDtmf(tones: String, durationMs: Int, interToneGapMs: Int): Boolean {
        return native.insertDtmf(tones, durationMs, interToneGapMs)
    }

    actual fun tones(): String = native.tones()
}
