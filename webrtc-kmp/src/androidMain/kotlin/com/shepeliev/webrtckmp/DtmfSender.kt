package com.shepeliev.webrtckmp

import org.webrtc.DtmfSender as NativeDtmfSender

public actual class DtmfSender(
    public val native: NativeDtmfSender,
) {
    public actual val canInsertDtmf: Boolean
        get() = native.canInsertDtmf()

    public actual val duration: Int
        get() = native.duration()

    public actual val interToneGap: Int
        get() = native.interToneGap()

    public actual fun insertDtmf(
        tones: String,
        durationMs: Int,
        interToneGapMs: Int,
    ): Boolean = native.insertDtmf(tones, durationMs, interToneGapMs)

    public actual fun tones(): String = native.tones()
}
