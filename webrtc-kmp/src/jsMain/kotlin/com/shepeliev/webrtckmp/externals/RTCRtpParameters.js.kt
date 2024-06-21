package com.shepeliev.webrtckmp.externals

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
internal actual val RTCRtpParameters.codes: List<RTCRtpCodecParameters>
    get() = (this as JsRTCRtpParameters).codecs.toList()

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
internal actual val RTCRtpParameters.headerExtensions: List<dynamic>
    get() = (this as JsRTCRtpParameters).headerExtensions.toList()

@JsName("RTCRtpParameters")
private external interface JsRTCRtpParameters : RTCRtcpParameters {
    val codecs: Array<RTCRtpCodecParameters>
    val headerExtensions: Array<dynamic>
}
