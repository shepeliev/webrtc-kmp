package com.shepeliev.webrtckmp

import org.webrtc.SessionDescription as AndroidSessionDescription

actual class SessionDescription internal constructor(val android: AndroidSessionDescription) {
    actual val type: SessionDescriptionType = android.type.asCommon()
    actual val sdp: String = android.description

    actual companion object {
        actual fun fromSdp(type: SessionDescriptionType, description: String): SessionDescription {
            return SessionDescription(
                AndroidSessionDescription(type.asAndroid(), description)
            )
        }
    }
}

private fun AndroidSessionDescription.Type.asCommon(): SessionDescriptionType {
    return when (this) {
        AndroidSessionDescription.Type.OFFER -> SessionDescriptionType.Offer
        AndroidSessionDescription.Type.PRANSWER -> SessionDescriptionType.Pranswer
        AndroidSessionDescription.Type.ANSWER -> SessionDescriptionType.Answer
        AndroidSessionDescription.Type.ROLLBACK -> SessionDescriptionType.Rollback
    }
}

private fun SessionDescriptionType.asAndroid(): AndroidSessionDescription.Type {
    return when (this) {
        SessionDescriptionType.Offer -> AndroidSessionDescription.Type.OFFER
        SessionDescriptionType.Pranswer -> AndroidSessionDescription.Type.PRANSWER
        SessionDescriptionType.Answer -> AndroidSessionDescription.Type.ANSWER
        SessionDescriptionType.Rollback -> AndroidSessionDescription.Type.ROLLBACK
    }
}
