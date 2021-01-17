package com.shepeliev.webrtckmm

expect class DtmfSender {
    val canInsertDtmf: Boolean
    val duration: Int
    val interToneGap: Int

    fun insertDtmf(tones: String, durationMs: Int = 300, interToneGapMs: Int = 50): Boolean
    fun tones(): String
    fun dispose()
}
