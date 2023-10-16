package com.shepeliev.webrtckmp

import org.webrtc.RtpReceiver as NativeRtpReceiver

actual class RtpReceiver(val native: NativeRtpReceiver, actual val track: MediaStreamTrack?) {
    actual val id: String
        get() = native.id()

    actual val parameters: RtpParameters
        get() = RtpParameters(native.parameters)

    actual fun getCapabilities(kind: MediaStreamTrackKind): RtpCapabilities? {
        require(kind in listOf(MediaStreamTrackKind.Audio, MediaStreamTrackKind.Video)) {
            "Unsupported track kind: $kind"
        }

        return WebRtc.peerConnectionFactory.getRtpReceiverCapabilities(kind.asNative())
            ?.let { RtpCapabilities(it) }
    }
}
