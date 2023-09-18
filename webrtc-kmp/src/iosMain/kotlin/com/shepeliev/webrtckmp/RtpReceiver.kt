package com.shepeliev.webrtckmp

import WebRTC.RTCRtpReceiver

actual class RtpReceiver(val native: RTCRtpReceiver, actual val track: MediaStreamTrack?) {
    actual val id: String
        get() = native.receiverId

    actual val parameters: RtpParameters
        get() = RtpParameters(native.parameters)

    actual fun getCapabilities(kind: MediaStreamTrackKind): RtpCapabilities? {
        require(kind in listOf(MediaStreamTrackKind.Audio, MediaStreamTrackKind.Video)) {
            "Unsupported track kind: $kind"
        }

        return WebRtc.peerConnectionFactory.rtpReceiverCapabilitiesFor(kind.asNative()).let {
            // TODO: fill headerExtensions
            RtpCapabilities(it, emptyList())
        }
    }
}
