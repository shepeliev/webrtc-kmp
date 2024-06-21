package com.shepeliev.webrtckmp

import com.shepeliev.webrtckmp.externals.RTCIceCandidate
import com.shepeliev.webrtckmp.internal.jsonStringify

actual class IceCandidate internal constructor(val js: RTCIceCandidate) {
    actual constructor(sdpMid: String, sdpMLineIndex: Int, candidate: String) : this(
        RTCIceCandidate(candidate, sdpMid, sdpMLineIndex)
    )

    actual val sdpMid: String = js.sdpMid
    actual val sdpMLineIndex: Int = js.sdpMLineIndex
    actual val candidate: String = js.candidate

    actual override fun toString(): String = js.jsonStringify()
}
