package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.RTCIceCandidate as NativeIceCandidate

actual class IceCandidate internal constructor(val native: NativeIceCandidate) {
    actual constructor(sdpMid: String, sdpMLineIndex: Int, candidate: String) : this(
        NativeIceCandidate(sdpMid, sdpMLineIndex, candidate)
    )

    actual val sdpMid: String = native.sdpMid
    actual val sdpMLineIndex: Int = native.sdpMLineIndex
    actual val candidate: String = native.sdp

    actual override fun toString(): String = native.toString()
}
