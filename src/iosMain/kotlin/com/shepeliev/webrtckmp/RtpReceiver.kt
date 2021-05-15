package com.shepeliev.webrtckmp

import WebRTC.RTCRtpReceiver

actual class RtpReceiver(val native: RTCRtpReceiver) {
    actual val id: String
        get() = native.receiverId

    actual val track: MediaStreamTrack?
        get() = native.track()?.let { MediaStreamTrack.createCommon(it, remote = true) }

    actual val parameters: RtpParameters
        get() = RtpParameters(native.parameters)
}
