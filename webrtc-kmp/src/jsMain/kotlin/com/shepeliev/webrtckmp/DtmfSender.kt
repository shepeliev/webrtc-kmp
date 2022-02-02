package com.shepeliev.webrtckmp

actual class DtmfSender(val js: RTCDTMFSender) {

    actual val canInsertDtmf: Boolean = true

    actual val duration: Int = -1

    actual val interToneGap: Int = -1

    actual fun insertDtmf(tones: String, durationMs: Int, interToneGapMs: Int): Boolean {
        js.insertDTMF(tones, durationMs.toLong(), interToneGapMs.toLong())
        return true
    }

    actual fun tones(): String = js.toneBuffer
}
