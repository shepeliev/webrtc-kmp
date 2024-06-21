package com.shepeliev.webrtckmp.externals

external interface RTCRtpReceiver {
    val track: PlatformMediaStreamTrack
    fun getParameters(): RTCRtpParameters
}
