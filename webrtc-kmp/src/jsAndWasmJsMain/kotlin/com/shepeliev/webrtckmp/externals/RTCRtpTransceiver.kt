package com.shepeliev.webrtckmp.externals

internal external interface RTCRtpTransceiver {
    val currentDirection: String?
    var direction: String
    val mid: String?
    val receiver: RTCRtpReceiver
    val sender: RTCRtpSender
    val stopped: Boolean

    fun stop()
}
