package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.RTCRtpReceiver

actual class RtpReceiver(
    val native: RTCRtpReceiver,
    actual val track: MediaStreamTrack?
) {
    actual val id: String
        get() = TODO()

    actual val parameters: RtpParameters
        get() = RtpParameters(native = native.parameters)
}
