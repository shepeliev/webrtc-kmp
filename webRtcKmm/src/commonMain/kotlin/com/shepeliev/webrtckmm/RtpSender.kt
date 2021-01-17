package com.shepeliev.webrtckmm

expect class RtpSender {

    val id: String
    val track: MediaStreamTrack?
    var streams: List<String>
    var parameters: RtpParameters
    val dtmf: DtmfSender?

    fun setTrack(track: MediaStreamTrack?, takeOwnership: Boolean): Boolean
    fun dispose()
}
