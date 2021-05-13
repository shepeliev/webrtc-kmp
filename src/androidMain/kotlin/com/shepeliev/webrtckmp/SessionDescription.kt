package com.shepeliev.webrtckmp

import org.webrtc.SessionDescription as NativeSessionDescription

actual class SessionDescription internal constructor(val native: NativeSessionDescription) {
    actual val type: SessionDescriptionType = native.type.asCommon()
    actual val sdp: String = native.description
}

private fun NativeSessionDescription.Type.asCommon(): SessionDescriptionType {
    return when (this) {
        NativeSessionDescription.Type.OFFER -> SessionDescriptionType.Offer
        NativeSessionDescription.Type.PRANSWER -> SessionDescriptionType.Pranswer
        NativeSessionDescription.Type.ANSWER -> SessionDescriptionType.Answer
        NativeSessionDescription.Type.ROLLBACK -> SessionDescriptionType.Rollback
    }
}
