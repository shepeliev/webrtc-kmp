package com.shepeliev.webrtckmm

import org.webrtc.IceCandidate as NativeIceCandidate

actual class IceCandidate constructor(val native: NativeIceCandidate) {

    actual constructor(
        sdpMid: String,
        sdpMLineIndex: Int,
        sdp: String,
    ) : this(NativeIceCandidate(sdpMid, sdpMLineIndex, sdp))

    actual val sdpMid: String = native.sdpMid
    actual val sdpMLineIndex: Int = native.sdpMLineIndex
    actual val sdp: String = native.sdp
    actual val serverUrl: String = native.serverUrl
    actual val adapterType: AdapterType = native.adapterType.asCommon()
}

internal fun NativeIceCandidate.asCommon(): IceCandidate = IceCandidate(this)
