package com.shepeliev.webrtckmp.externals

external interface RTCRtpParameters {
    val rtcp: RTCRtcpParameters
}

internal expect val RTCRtpParameters.codes: List<RTCRtpCodecParameters>
internal expect val RTCRtpParameters.headerExtensions: List<Any>
