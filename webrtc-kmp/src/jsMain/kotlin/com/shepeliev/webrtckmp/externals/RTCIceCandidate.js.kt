package com.shepeliev.webrtckmp.externals

import kotlin.js.json

internal actual fun RTCIceCandidate(
    candidate: String,
    sdpMid: String,
    sdpMLineIndex: Int
): RTCIceCandidate {
    return JsRTCIceCandidate(
        json(
            "sdpMid" to sdpMid,
            "sdpMLineIndex" to sdpMLineIndex,
            "candidate" to candidate
        )
    )
}

@JsName("RTCIceCandidate")
private external class JsRTCIceCandidate(options: dynamic) : RTCIceCandidate {
    override val candidate: String
    override val sdpMid: String
    override val sdpMLineIndex: Int
}
