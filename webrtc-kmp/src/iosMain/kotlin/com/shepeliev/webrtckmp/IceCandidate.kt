@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.shepeliev.webrtckmp

import WebRTC.RTCIceCandidate

actual class IceCandidate internal constructor(val native: RTCIceCandidate) {
    actual constructor(sdpMid: String, sdpMLineIndex: Int, candidate: String) : this(
        RTCIceCandidate(candidate, sdpMLineIndex, sdpMid)
    )

    actual val sdpMid: String = native.sdpMid!!
    actual val sdpMLineIndex: Int = native.sdpMLineIndex
    actual val candidate: String = native.sdp

    actual override fun toString(): String = candidate
}
