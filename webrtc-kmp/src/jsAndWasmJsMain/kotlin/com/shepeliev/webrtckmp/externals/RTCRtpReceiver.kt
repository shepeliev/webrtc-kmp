package com.shepeliev.webrtckmp.externals

internal external interface RTCRtpReceiver {
    val track: PlatformMediaStreamTrack

    fun getParameters(): RTCRtpParameters
}
