package com.shepeliev.webrtckmp

import org.webrtc.IceCandidate as NativeIceCandidate

public actual class IceCandidate internal constructor(
    public val native: NativeIceCandidate,
) {
    public actual constructor(sdpMid: String, sdpMLineIndex: Int, candidate: String) : this(
        NativeIceCandidate(sdpMid, sdpMLineIndex, candidate),
    )

    public actual val sdpMid: String = native.sdpMid
    public actual val sdpMLineIndex: Int = native.sdpMLineIndex
    public actual val candidate: String = native.sdp

    actual override fun toString(): String = native.toString()
}
