package com.shepeliev.webrtckmp

import WebRTC.RTCRtpTransceiver
import WebRTC.RTCRtpTransceiverDirection
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr

actual class RtpTransceiver(
    val native: RTCRtpTransceiver,
    private val senderTrack: MediaStreamTrack?,
    private val receiverTrack: MediaStreamTrack?,
) {

    actual var direction: RtpTransceiverDirection
        get() = rtcRtpTransceiverDirectionAsCommon(native.direction)
        set(value) {
            native.setDirection(value.asNative(), null)
        }

    actual val currentDirection: RtpTransceiverDirection?
        get() = memScoped {
            val d = alloc<RTCRtpTransceiverDirection.Var>()
            native.currentDirection(d.ptr)
            rtcRtpTransceiverDirectionAsCommon(d.value)
        }

    actual val mid: String
        get() = native.mid

    actual val sender: RtpSender
        get() = RtpSender(native.sender, senderTrack)

    actual val receiver: RtpReceiver
        get() = RtpReceiver(native.receiver, receiverTrack)

    actual val stopped: Boolean
        get() = native.isStopped

    actual fun stop() = native.stopInternal()
}

private fun rtcRtpTransceiverDirectionAsCommon(direction: RTCRtpTransceiverDirection): RtpTransceiverDirection {
    return when (direction) {
        RTCRtpTransceiverDirection.RTCRtpTransceiverDirectionSendRecv -> {
            RtpTransceiverDirection.SendRecv
        }

        RTCRtpTransceiverDirection.RTCRtpTransceiverDirectionSendOnly -> {
            RtpTransceiverDirection.SendOnly
        }

        RTCRtpTransceiverDirection.RTCRtpTransceiverDirectionRecvOnly -> {
            RtpTransceiverDirection.RecvOnly
        }

        RTCRtpTransceiverDirection.RTCRtpTransceiverDirectionInactive -> {
            RtpTransceiverDirection.Inactive
        }

        RTCRtpTransceiverDirection.RTCRtpTransceiverDirectionStopped -> {
            RtpTransceiverDirection.Stopped
        }

        else -> error("Unknown RTCRtpTransceiverDirection: $direction")
    }
}

internal fun RtpTransceiverDirection.asNative(): RTCRtpTransceiverDirection {
    return when (this) {
        RtpTransceiverDirection.SendRecv -> {
            RTCRtpTransceiverDirection.RTCRtpTransceiverDirectionSendRecv
        }

        RtpTransceiverDirection.SendOnly -> {
            RTCRtpTransceiverDirection.RTCRtpTransceiverDirectionSendOnly
        }

        RtpTransceiverDirection.RecvOnly -> {
            RTCRtpTransceiverDirection.RTCRtpTransceiverDirectionRecvOnly
        }

        RtpTransceiverDirection.Inactive -> {
            RTCRtpTransceiverDirection.RTCRtpTransceiverDirectionInactive
        }
        RtpTransceiverDirection.Stopped -> {
            RTCRtpTransceiverDirection.RTCRtpTransceiverDirectionStopped
        }
    }
}
