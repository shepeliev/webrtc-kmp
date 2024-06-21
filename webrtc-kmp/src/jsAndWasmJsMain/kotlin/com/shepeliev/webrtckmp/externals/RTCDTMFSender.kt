package com.shepeliev.webrtckmp.externals

external interface RTCDTMFSender {
    val toneBuffer: String
    fun insertDTMF(tones: String, duration: Long, interToneGap: Long)
}
