package com.shepeliev.webrtckmp.externals

import com.shepeliev.webrtckmp.internal.toList

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
internal actual val RTCRtpParameters.codes: List<RTCRtpCodecParameters>
    get() = (this as WasmJsRTCRtpParameters).codecs.toList().filterNotNull()

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
internal actual val RTCRtpParameters.headerExtensions: List<Any>
    get() = (this as WasmJsRTCRtpParameters).headerExtensions.toList().filterNotNull()

@JsName("RTCRtpParameters")
private external interface WasmJsRTCRtpParameters : RTCRtcpParameters {
    val codecs: JsArray<WasmJsRTCRtpCodecParameters>
    val headerExtensions: JsArray<JsAny>
}

@JsName("RTCRtpCodecParameters")
private external interface WasmJsRTCRtpCodecParameters : RTCRtpCodecParameters, JsAny
