package com.shepeliev.webrtckmp.externals

internal external interface RTCIceCandidate {
    val candidate: String
    val sdpMid: String
    val sdpMLineIndex: Int
}

internal expect fun RTCIceCandidate(
    candidate: String,
    sdpMid: String,
    sdpMLineIndex: Int,
): RTCIceCandidate
