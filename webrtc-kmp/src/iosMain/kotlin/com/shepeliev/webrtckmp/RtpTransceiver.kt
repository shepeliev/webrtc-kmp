@file:OptIn(ExperimentalForeignApi::class)

package com.shepeliev.webrtckmp

import WebRTC.RTCRtpTransceiver
import WebRTC.RTCRtpTransceiverDirection
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr

public actual class RtpTransceiver(
    public val native: RTCRtpTransceiver,
    private val senderTrack: MediaStreamTrack?,
    private val receiverTrack: MediaStreamTrack?,
) {
    public actual var direction: RtpTransceiverDirection
        get() = rtcRtpTransceiverDirectionAsCommon(native.direction)
        set(value) {
            native.setDirection(value.asNative(), null)
        }

    public actual val currentDirection: RtpTransceiverDirection?
        get() =
            memScoped {
                val d = alloc<RTCRtpTransceiverDirection.Var>()
                native.currentDirection(d.ptr)
                rtcRtpTransceiverDirectionAsCommon(d.value)
            }

    public actual val mid: String get() = native.mid
    public actual val sender: RtpSender get() = RtpSender(native.sender, senderTrack)
    public actual val receiver: RtpReceiver get() = RtpReceiver(native.receiver, receiverTrack)
    public actual val stopped: Boolean get() = native.isStopped

    public actual fun stop() {
        native.stopInternal()
    }
}

private fun rtcRtpTransceiverDirectionAsCommon(
    direction: RTCRtpTransceiverDirection,
): RtpTransceiverDirection =
    when (direction) {
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

internal fun RtpTransceiverDirection.asNative(): RTCRtpTransceiverDirection =
    when (this) {
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
