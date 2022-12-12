package com.shepeliev.webrtckmp

actual class DtmfSender() {

    actual val canInsertDtmf: Boolean
        get() = false

    actual val duration: Int
        get() = 0

    actual val interToneGap: Int
        get() = 0

    actual fun insertDtmf(tones: String, durationMs: Int, interToneGapMs: Int): Boolean {
        TODO("Not yet implemented for JVM platform")
    }

    actual fun tones(): String = ""
}
