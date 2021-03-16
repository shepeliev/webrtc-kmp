package com.shepeliev.webrtckmm

import org.webrtc.RtpTransceiver as NativeRtpTransceiver

actual class RtpTransceiver(val native: NativeRtpTransceiver) {
    actual var direction: RtpTransceiverDirection
        get() = native.direction.asCommon()
        set(value) {
            native.direction = value.asNative()
        }

    actual val currentDirectioin: RtpTransceiverDirection?
        get() = native.currentDirection?.asCommon()

    actual val mediaType: MediaStreamTrack.MediaType
        get() = native.mediaType.asCommon()

    actual val mid: String
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

internal fun NativeRtpTransceiver.asCommon() = RtpTransceiver(this)

internal fun NativeRtpTransceiver.RtpTransceiverDirection.asCommon(): RtpTransceiverDirection {
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

internal fun RtpTransceiverDirection.asNative(): NativeRtpTransceiver.RtpTransceiverDirection {
    return when (this) {
        RtpTransceiverDirection.SendRecv -> NativeRtpTransceiver.RtpTransceiverDirection.SEND_RECV
        RtpTransceiverDirection.SendOnly -> NativeRtpTransceiver.RtpTransceiverDirection.SEND_ONLY
        RtpTransceiverDirection.RecvOnly -> NativeRtpTransceiver.RtpTransceiverDirection.RECV_ONLY
        RtpTransceiverDirection.Inactive -> NativeRtpTransceiver.RtpTransceiverDirection.INACTIVE
        RtpTransceiverDirection.Stopped -> NativeRtpTransceiver.RtpTransceiverDirection.INACTIVE
    }
}
