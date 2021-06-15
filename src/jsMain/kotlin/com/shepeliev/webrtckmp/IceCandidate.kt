package com.shepeliev.webrtckmp

actual class IceCandidate internal constructor(val js: RTCIceCandidate) {
    actual val sdpMid: String = js.sdpMid
    actual val sdpMLineIndex: Int = js.sdpMLineIndex
    actual val candidate: String = js.candidate

    actual override fun toString(): String = JSON.stringify(js)
}
