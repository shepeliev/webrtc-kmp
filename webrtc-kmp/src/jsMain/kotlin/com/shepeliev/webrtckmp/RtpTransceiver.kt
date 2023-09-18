package com.shepeliev.webrtckmp

actual class RtpTransceiver(val native: RTCRtpTransceiver) {
    actual var direction: RtpTransceiverDirection
        get() = native.direction.toRtpTransceiverDirection()
        set(value) {
            native.direction = value.toJs()
        }

    actual val currentDirection: RtpTransceiverDirection?
        get() = native.currentDirection?.toRtpTransceiverDirection()

    actual val mid: String
        get() = native.mid ?: ""

    actual val sender: RtpSender
        get() = RtpSender(native.sender)

    actual val receiver: RtpReceiver
        get() = RtpReceiver(native.receiver)

    actual val stopped: Boolean
        get() = native.stopped

    actual fun setCodecPreferences(capabilities: List<RtpCapabilities.CodecCapability>) {
        native.setCodecPreferences(capabilities.map { it.native }.toTypedArray())
    }

    actual fun stop() = native.stop()

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
