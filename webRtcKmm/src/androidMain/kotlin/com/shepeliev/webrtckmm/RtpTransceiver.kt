package com.shepeliev.webrtckmm

import org.webrtc.RtpTransceiver as NativeRtpTransceiver

actual class RtpTransceiver(val native: NativeRtpTransceiver) {
    actual var direction: RtpTransceiverDirection
        get() = native.direction.toCommon()
        set(value) {
            native.direction = value.toNative()
        }

    actual val currentDirectioin: RtpTransceiverDirection?
        get() = native.currentDirection?.toCommon()

    actual val mediaType: MediaStreamTrack.MediaType
        get() = native.mediaType.toCommon()

    actual val mid: String?
        get() = native.mid

    actual val sender: RtpSender
        get() = RtpSender(native.sender)

    actual val receiver: RtpReceiver
        get() = RtpReceiver(native.receiver)

    actual val isStopped: Boolean
        get() = native.isStopped

    actual fun stop() = native.stop()
    actual fun dispose() = native.dispose()
}

private fun NativeRtpTransceiver.RtpTransceiverDirection.toCommon(): RtpTransceiverDirection {
    return when (this) {
        NativeRtpTransceiver.RtpTransceiverDirection.SEND_RECV -> {
            RtpTransceiverDirection.SendRecv
        }

        NativeRtpTransceiver.RtpTransceiverDirection.SEND_ONLY -> {
            RtpTransceiverDirection.SendOnly
        }

        NativeRtpTransceiver.RtpTransceiverDirection.RECV_ONLY -> {
            RtpTransceiverDirection.RecvOnly
        }

        NativeRtpTransceiver.RtpTransceiverDirection.INACTIVE -> {
            RtpTransceiverDirection.Inactive
        }
    }
}

private fun RtpTransceiverDirection.toNative(): NativeRtpTransceiver.RtpTransceiverDirection {
    return when (this) {
        RtpTransceiverDirection.SendRecv -> NativeRtpTransceiver.RtpTransceiverDirection.SEND_RECV
        RtpTransceiverDirection.SendOnly -> NativeRtpTransceiver.RtpTransceiverDirection.SEND_ONLY
        RtpTransceiverDirection.RecvOnly -> NativeRtpTransceiver.RtpTransceiverDirection.RECV_ONLY
        RtpTransceiverDirection.Inactive -> NativeRtpTransceiver.RtpTransceiverDirection.INACTIVE
    }
}
