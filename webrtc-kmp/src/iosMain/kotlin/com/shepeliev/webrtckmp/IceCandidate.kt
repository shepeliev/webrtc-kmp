@file:OptIn(ExperimentalForeignApi::class)

package com.shepeliev.webrtckmp

import kotlinx.cinterop.ExperimentalForeignApi
import swiftPMImport.com.shepeliev.webrtc.kmp.RTCIceCandidate

public actual class IceCandidate internal constructor(
    public val native: RTCIceCandidate,
) {
    public actual constructor(sdpMid: String, sdpMLineIndex: Int, candidate: String) : this(
        RTCIceCandidate(candidate, sdpMLineIndex, sdpMid),
    )

    public actual val sdpMid: String = native.sdpMid!!
    public actual val sdpMLineIndex: Int = native.sdpMLineIndex
    public actual val candidate: String = native.sdp

    actual override fun toString(): String = candidate
}
