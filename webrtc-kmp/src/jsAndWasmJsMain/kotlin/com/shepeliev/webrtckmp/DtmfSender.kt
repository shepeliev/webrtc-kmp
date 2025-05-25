package com.shepeliev.webrtckmp

import com.shepeliev.webrtckmp.externals.RTCDTMFSender

public actual class DtmfSender internal constructor(
    private val js: RTCDTMFSender,
) {
    public actual val canInsertDtmf: Boolean = true
    public actual val duration: Int = -1
    public actual val interToneGap: Int = -1

    public actual fun insertDtmf(
        tones: String,
        durationMs: Int,
        interToneGapMs: Int,
    ): Boolean {
        js.insertDTMF(tones, durationMs.toLong(), interToneGapMs.toLong())
        return true
    }

    public actual fun tones(): String = js.toneBuffer
}
