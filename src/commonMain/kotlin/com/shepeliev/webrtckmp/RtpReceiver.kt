package com.shepeliev.webrtckmp

expect class RtpReceiver {
    val id: String
    val track: MediaStreamTrack?
    val parameters: RtpParameters

    fun setObserver(observer: RtpReceiverObserver)
    fun dispose()
}

fun interface RtpReceiverObserver {
    fun onFirstPacketReceived(mediaType: MediaStreamTrack.MediaType)
}
