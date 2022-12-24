package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.RTCIceCandidate

actual class IceCandidate internal constructor(val native: RTCIceCandidate) {
    actual constructor(sdpMid: String, sdpMLineIndex: Int, candidate: String) : this(
        RTCIceCandidate(sdpMid, sdpMLineIndex, candidate)
    )

    actual val sdpMid: String = native.sdpMid
    actual val sdpMLineIndex: Int = native.sdpMLineIndex
    actual val candidate: String = native.sdp

    actual override fun toString(): String = native.toString()
}
