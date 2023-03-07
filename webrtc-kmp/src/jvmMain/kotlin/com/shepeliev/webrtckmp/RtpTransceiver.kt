package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.RTCRtpTransceiver
import dev.onvoid.webrtc.RTCRtpTransceiverDirection

actual class RtpTransceiver(
    val native: RTCRtpTransceiver,
    private val senderTrack: MediaStreamTrack?,
) {
    actual val currentDirection: RtpTransceiverDirection?
        get() = native.currentDirection.asCommon()

    actual var direction: RtpTransceiverDirection
        get() = native.direction.asCommon()
        set(value) {
            native.direction = value.asNative()
        }

    actual val mid: String
        get() = native.mid

    actual val receiver: RtpReceiver
        get() = RtpReceiver(native.receiver, senderTrack)

    actual val sender: RtpSender
        get() = RtpSender(native = native.sender, track = senderTrack)

    actual val stopped: Boolean
        get() = native.stopped()

    actual fun stop() = native.stop()
}

private fun RTCRtpTransceiverDirection.asCommon(): RtpTransceiverDirection {
    return when (this) {
        RTCRtpTransceiverDirection.SEND_ONLY -> RtpTransceiverDirection.SendOnly
        RTCRtpTransceiverDirection.RECV_ONLY -> RtpTransceiverDirection.RecvOnly
        RTCRtpTransceiverDirection.INACTIVE -> RtpTransceiverDirection.Inactive
        RTCRtpTransceiverDirection.SEND_RECV -> RtpTransceiverDirection.SendRecv
        RTCRtpTransceiverDirection.STOPPED -> RtpTransceiverDirection.Stopped
    }
}

private fun RtpTransceiverDirection.asNative(): RTCRtpTransceiverDirection {
    return when (this) {
        RtpTransceiverDirection.SendOnly -> RTCRtpTransceiverDirection.SEND_ONLY
        RtpTransceiverDirection.RecvOnly -> RTCRtpTransceiverDirection.RECV_ONLY
        RtpTransceiverDirection.Inactive -> RTCRtpTransceiverDirection.INACTIVE
        RtpTransceiverDirection.SendRecv -> RTCRtpTransceiverDirection.SEND_RECV
        RtpTransceiverDirection.Stopped -> RTCRtpTransceiverDirection.STOPPED
    }
}
