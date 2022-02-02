package com.shepeliev.webrtckmp

actual class RtpReceiver(val js: RTCRtpReceiver) {
    actual val id: String
        get() = js.track.id

    actual val track: MediaStreamTrack?
        get() = js.track.asCommon()

    actual val parameters: RtpParameters
        get() = RtpParameters(js.getParameters())
}
