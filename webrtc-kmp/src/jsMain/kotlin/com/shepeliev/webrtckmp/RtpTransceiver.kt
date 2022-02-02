package com.shepeliev.webrtckmp

actual class RtpTransceiver(val js: RTCRtpTransceiver) {
    actual var direction: RtpTransceiverDirection
        get() = js.direction.toRtpTransceiverDirection()
        set(value) {
            js.direction = value.toJs()
        }

    actual val currentDirection: RtpTransceiverDirection?
        get() = js.currentDirection?.toRtpTransceiverDirection()

    actual val mid: String
        get() = js.mid ?: ""

    actual val sender: RtpSender
        get() = RtpSender(js.sender)

    actual val receiver: RtpReceiver
        get() = RtpReceiver(js.receiver)

    actual val stopped: Boolean
        get() = js.stopped

    actual fun stop() = js.stop()

    private fun String.toRtpTransceiverDirection(): RtpTransceiverDirection = when (this) {
        "sendrecv" -> RtpTransceiverDirection.SendRecv
        "sendonly" -> RtpTransceiverDirection.SendOnly
        "recvonly" -> RtpTransceiverDirection.RecvOnly
        "inactive" -> RtpTransceiverDirection.Inactive
        else -> throw IllegalArgumentException("Illegal direction: $this")
    }

    private fun RtpTransceiverDirection.toJs(): String = when (this) {
        RtpTransceiverDirection.SendRecv -> "sendrecv"
        RtpTransceiverDirection.SendOnly -> "sendonly"
        RtpTransceiverDirection.RecvOnly -> "recvonly"
        RtpTransceiverDirection.Inactive -> "inactive"
        RtpTransceiverDirection.Stopped -> "inactive"
    }
}
