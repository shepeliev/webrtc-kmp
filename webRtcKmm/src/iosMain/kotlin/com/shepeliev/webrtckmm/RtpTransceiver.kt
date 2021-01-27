package com.shepeliev.webrtckmm

import cocoapods.GoogleWebRTC.RTCRtpTransceiver
import cocoapods.GoogleWebRTC.RTCRtpTransceiverDirection
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr

actual class RtpTransceiver(val native: RTCRtpTransceiver) {
    actual var direction: RtpTransceiverDirection
        get() = rtcRtpTransceiverDirectionAsCommon(native.direction)
        set(value) {
            native.direction = value.asNative()
        }

    actual val currentDirectioin: RtpTransceiverDirection?
        get() = memScoped {
            val d = alloc<RTCRtpTransceiverDirection.Var>()
            native.currentDirection(d.ptr)
            rtcRtpTransceiverDirectionAsCommon(d.value)
        }

    actual val mediaType: MediaStreamTrack.MediaType
        get() = rtcRtpMediaTypeAsCommon(native.mediaType)

    actual val mid: String
        get() = native.mid

    actual val sender: RtpSender
        get() = RtpSender(native.sender)

    actual val receiver: RtpReceiver
        get() = RtpReceiver(native.receiver)

    actual val isStopped: Boolean
        get() = native.isStopped

    actual fun stop() = native.stop()

    actual fun dispose() {
        // not applicable
    }
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
    }
}
