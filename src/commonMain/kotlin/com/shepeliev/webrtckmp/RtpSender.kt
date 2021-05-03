package com.shepeliev.webrtckmp

expect class RtpSender {
    val id: String
    val track: MediaStreamTrack?
    var parameters: RtpParameters
    val dtmf: DtmfSender?

    fun setTrack(track: MediaStreamTrack?, takeOwnership: Boolean): Boolean
}
