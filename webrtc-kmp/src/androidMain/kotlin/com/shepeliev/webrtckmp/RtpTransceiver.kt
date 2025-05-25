package com.shepeliev.webrtckmp

import org.webrtc.RtpTransceiver as AndroidRtpTransceiver

public actual class RtpTransceiver(
    public val native: AndroidRtpTransceiver,
    private val senderTrack: MediaStreamTrack?,
    private val receiverTrack: MediaStreamTrack?,
) {
    public actual var direction: RtpTransceiverDirection
        get() = native.direction.asCommon()
        set(value) {
            native.direction = value.toPlatform()
        }

    public actual val currentDirection: RtpTransceiverDirection?
        get() = native.currentDirection?.asCommon()

    public actual val mid: String get() = native.mid
    public actual val sender: RtpSender get() = RtpSender(native.sender, senderTrack)
    public actual val receiver: RtpReceiver get() = RtpReceiver(native.receiver, receiverTrack)
    public actual val stopped: Boolean get() = native.isStopped

    public actual fun stop() {
        native.stop()
    }
}

private fun AndroidRtpTransceiver.RtpTransceiverDirection.asCommon(): RtpTransceiverDirection =
    when (this) {
        AndroidRtpTransceiver.RtpTransceiverDirection.SEND_RECV -> {
            RtpTransceiverDirection.SendRecv
        }

        AndroidRtpTransceiver.RtpTransceiverDirection.SEND_ONLY -> {
            RtpTransceiverDirection.SendOnly
        }

        AndroidRtpTransceiver.RtpTransceiverDirection.RECV_ONLY -> {
            RtpTransceiverDirection.RecvOnly
        }

        AndroidRtpTransceiver.RtpTransceiverDirection.INACTIVE -> {
            RtpTransceiverDirection.Inactive
        }

        AndroidRtpTransceiver.RtpTransceiverDirection.STOPPED -> {
            RtpTransceiverDirection.Stopped
        }
    }

internal fun RtpTransceiverDirection.toPlatform(): AndroidRtpTransceiver.RtpTransceiverDirection =
    when (this) {
        RtpTransceiverDirection.SendRecv -> AndroidRtpTransceiver.RtpTransceiverDirection.SEND_RECV
        RtpTransceiverDirection.SendOnly -> AndroidRtpTransceiver.RtpTransceiverDirection.SEND_ONLY
        RtpTransceiverDirection.RecvOnly -> AndroidRtpTransceiver.RtpTransceiverDirection.RECV_ONLY
        RtpTransceiverDirection.Inactive -> AndroidRtpTransceiver.RtpTransceiverDirection.INACTIVE
        RtpTransceiverDirection.Stopped -> AndroidRtpTransceiver.RtpTransceiverDirection.STOPPED
    }
