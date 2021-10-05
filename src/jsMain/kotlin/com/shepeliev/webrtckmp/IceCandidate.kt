package com.shepeliev.webrtckmp

import kotlin.js.json

actual class IceCandidate internal constructor(val js: RTCIceCandidate) {
    actual constructor(sdpMid: String, sdpMLineIndex: Int, candidate: String) : this(
        RTCIceCandidate(
            json(
                "sdpMid" to sdpMid,
                "sdpMLineIndex" to sdpMLineIndex,
                "candidate" to candidate
            )
        )
    )

    actual val sdpMid: String = js.sdpMid
    actual val sdpMLineIndex: Int = js.sdpMLineIndex
    actual val candidate: String = js.candidate

    actual override fun toString(): String = JSON.stringify(js)
}
