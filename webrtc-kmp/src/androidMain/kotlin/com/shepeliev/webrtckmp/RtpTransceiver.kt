package com.shepeliev.webrtckmp

import org.webrtc.RtpTransceiver as AndroidRtpTransceiver

actual class RtpTransceiver(
    val native: AndroidRtpTransceiver,
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
        get() = native.isStopped

    actual fun setCodecPreferences(capabilities: List<RtpCapabilities.CodecCapability>) {
        native.setCodecPreferences(capabilities.map { it.native })
    }

    actual fun stop() = native.stop()
}

private fun AndroidRtpTransceiver.RtpTransceiverDirection.asCommon(): RtpTransceiverDirection {
    return when (this) {
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
}

internal fun RtpTransceiverDirection.asNative(): AndroidRtpTransceiver.RtpTransceiverDirection {
    return when (this) {
        RtpTransceiverDirection.SendRecv -> AndroidRtpTransceiver.RtpTransceiverDirection.SEND_RECV
        RtpTransceiverDirection.SendOnly -> AndroidRtpTransceiver.RtpTransceiverDirection.SEND_ONLY
        RtpTransceiverDirection.RecvOnly -> AndroidRtpTransceiver.RtpTransceiverDirection.RECV_ONLY
        RtpTransceiverDirection.Inactive -> AndroidRtpTransceiver.RtpTransceiverDirection.INACTIVE
        RtpTransceiverDirection.Stopped -> AndroidRtpTransceiver.RtpTransceiverDirection.STOPPED
    }
}
