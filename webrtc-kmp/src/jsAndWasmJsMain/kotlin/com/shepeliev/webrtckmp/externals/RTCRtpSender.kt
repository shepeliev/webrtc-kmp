package com.shepeliev.webrtckmp.externals

internal external interface RTCRtpSender {
    val dtmf: RTCDTMFSender?
    val track: PlatformMediaStreamTrack?

    fun getParameters(): RTCRtpParameters
    fun setParameters(parameters: RTCRtpParameters)
}

internal expect suspend fun RTCRtpSender.replaceTrack(withTrack: PlatformMediaStreamTrack?)
