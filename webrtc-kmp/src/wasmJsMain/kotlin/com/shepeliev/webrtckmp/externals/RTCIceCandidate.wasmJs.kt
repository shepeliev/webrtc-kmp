package com.shepeliev.webrtckmp.externals

internal actual fun RTCIceCandidate(
    candidate: String,
    sdpMid: String,
    sdpMLineIndex: Int
): RTCIceCandidate {
    return createRTCIceCandidate(candidate, sdpMid, sdpMLineIndex)
}

@Suppress("UNUSED_PARAMETER")
private fun createRTCIceCandidate(
    candidate: String,
    sdpMid: String,
    sdpMLineIndex: Int
): WasmRTCIceCandidate = js("new RTCIceCandidate({candidate: candidate, sdpMid: sdpMid, sdpMLineIndex: sdpMLineIndex})")

internal external interface WasmRTCIceCandidate : JsAny, RTCIceCandidate
