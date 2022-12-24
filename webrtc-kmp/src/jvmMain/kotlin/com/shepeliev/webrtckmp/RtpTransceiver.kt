package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.RTCRtpTransceiverDirection

actual class RtpTransceiver(
    val native: dev.onvoid.webrtc.RTCRtpTransceiver,
    private val senderTrack: MediaStreamTrack?,
    private val receiverTrack: MediaStreamTrack?,
) {

    actual var direction: RtpTransceiverDirection
        get() = native.direction.asCommon()
        set(value) {
            native.direction = value.asNative()
        }

    actual val currentDirection: RtpTransceiverDirection?
        get() = native.currentDirection?.asCommon()

    actual val mid: String
        get() = native.mid

    actual val sender: RtpSender
        get() = RtpSender(native.sender, senderTrack)

    actual val receiver: RtpReceiver
        get() = RtpReceiver(native.receiver, receiverTrack)

    actual val stopped: Boolean
        get() = native.stopped()

    actual fun stop() = native.stop()
}

private fun RTCRtpTransceiverDirection.asCommon(): RtpTransceiverDirection {
    return when (this) {
        RTCRtpTransceiverDirection.SEND_RECV -> {
            RtpTransceiverDirection.SendRecv
        }

        RTCRtpTransceiverDirection.SEND_ONLY -> {
            RtpTransceiverDirection.SendOnly
        }

        RTCRtpTransceiverDirection.RECV_ONLY -> {
            RtpTransceiverDirection.RecvOnly
        }

        RTCRtpTransceiverDirection.INACTIVE -> {
            RtpTransceiverDirection.Inactive
        }

        RTCRtpTransceiverDirection.STOPPED -> {
            RtpTransceiverDirection.Stopped
        }
    }
}

internal fun RtpTransceiverDirection.asNative(): RTCRtpTransceiverDirection {
    return when (this) {
        RtpTransceiverDirection.SendRecv -> RTCRtpTransceiverDirection.SEND_RECV
        RtpTransceiverDirection.SendOnly -> RTCRtpTransceiverDirection.SEND_ONLY
        RtpTransceiverDirection.RecvOnly -> RTCRtpTransceiverDirection.RECV_ONLY
        RtpTransceiverDirection.Inactive -> RTCRtpTransceiverDirection.INACTIVE
        RtpTransceiverDirection.Stopped -> RTCRtpTransceiverDirection.INACTIVE
    }
}
