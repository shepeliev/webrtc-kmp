package com.shepeliev.webrtckmm.utils

import com.shepeliev.webrtckmm.IceCandidate
import org.webrtc.IceCandidate as NativeIceCandidate

fun NativeIceCandidate.toCommon(): IceCandidate {
    return IceCandidate(sdpMid, sdpMLineIndex, sdp, serverUrl, adapterType.toCommon())
}

fun IceCandidate.toNative(): NativeIceCandidate {
    return NativeIceCandidate(sdpMid, sdpMLineIndex, sdp)
}
