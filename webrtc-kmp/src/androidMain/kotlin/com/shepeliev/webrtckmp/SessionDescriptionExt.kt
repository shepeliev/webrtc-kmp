package com.shepeliev.webrtckmp

import org.webrtc.SessionDescription as AndroidSessionDescription

internal fun SessionDescription.asAndroid(): AndroidSessionDescription {
    return AndroidSessionDescription(type.asAndroid(), sdp)
}

private fun SessionDescriptionType.asAndroid(): AndroidSessionDescription.Type {
    return when (this) {
        SessionDescriptionType.Offer -> AndroidSessionDescription.Type.OFFER
        SessionDescriptionType.Pranswer -> AndroidSessionDescription.Type.PRANSWER
        SessionDescriptionType.Answer -> AndroidSessionDescription.Type.ANSWER
        SessionDescriptionType.Rollback -> AndroidSessionDescription.Type.ROLLBACK
    }
}

internal fun AndroidSessionDescription.asCommon(): SessionDescription {
    return SessionDescription(type.asCommon(), description)
}

private fun AndroidSessionDescription.Type.asCommon(): SessionDescriptionType {
    return when (this) {
        AndroidSessionDescription.Type.OFFER -> SessionDescriptionType.Offer
        AndroidSessionDescription.Type.PRANSWER -> SessionDescriptionType.Pranswer
        AndroidSessionDescription.Type.ANSWER -> SessionDescriptionType.Answer
        AndroidSessionDescription.Type.ROLLBACK -> SessionDescriptionType.Rollback
    }
}
