package com.shepeliev.webrtckmm

expect class RtpTransceiver {
    var direction: RtpTransceiverDirection
    val currentDirectioin: RtpTransceiverDirection?
    val mediaType: MediaStreamTrack.MediaType
    val mid: String
    val sender: RtpSender
    val receiver: RtpReceiver
    val isStopped: Boolean

    fun stop()
}

enum class RtpTransceiverDirection { SendRecv, SendOnly, RecvOnly, Inactive, Stopped; }
