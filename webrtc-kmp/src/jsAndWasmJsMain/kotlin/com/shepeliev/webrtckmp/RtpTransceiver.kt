package com.shepeliev.webrtckmp

import com.shepeliev.webrtckmp.externals.RTCRtpTransceiver

public actual class RtpTransceiver internal constructor(
    internal val js: RTCRtpTransceiver,
) {
    public actual var direction: RtpTransceiverDirection
        get() = js.direction.toRtpTransceiverDirection()
        set(value) {
            js.direction = value.toJs()
        }

    public actual val currentDirection: RtpTransceiverDirection?
        get() = js.currentDirection?.toRtpTransceiverDirection()

    public actual val mid: String get() = js.mid ?: ""
    public actual val sender: RtpSender get() = RtpSender(js.sender)
    public actual val receiver: RtpReceiver get() = RtpReceiver(js.receiver)
    public actual val stopped: Boolean get() = js.stopped

    public actual fun stop() {
        js.stop()
    }

    private fun String.toRtpTransceiverDirection(): RtpTransceiverDirection =
        when (this) {
            "sendrecv" -> RtpTransceiverDirection.SendRecv
            "sendonly" -> RtpTransceiverDirection.SendOnly
            "recvonly" -> RtpTransceiverDirection.RecvOnly
            "inactive" -> RtpTransceiverDirection.Inactive
            else -> throw IllegalArgumentException("Illegal direction: $this")
        }

    private fun RtpTransceiverDirection.toJs(): String =
        when (this) {
            RtpTransceiverDirection.SendRecv -> "sendrecv"
            RtpTransceiverDirection.SendOnly -> "sendonly"
            RtpTransceiverDirection.RecvOnly -> "recvonly"
            RtpTransceiverDirection.Inactive -> "inactive"
            RtpTransceiverDirection.Stopped -> "inactive"
        }
}
