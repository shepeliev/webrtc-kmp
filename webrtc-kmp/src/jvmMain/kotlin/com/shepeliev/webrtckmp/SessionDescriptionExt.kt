package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.RTCSdpType
import org.webrtc.SessionDescription as AndroidSessionDescription

internal fun SessionDescription.asPlatform(): AndroidSessionDescription {
    return AndroidSessionDescription(type.asPlatform(), sdp)
}

private fun SessionDescriptionType.asPlatform(): RTCSdpType {
    return when (this) {
        SessionDescriptionType.Offer -> RTCSdpType.OFFER
        SessionDescriptionType.Pranswer -> RTCSdpType.PR_ANSWER
        SessionDescriptionType.Answer -> RTCSdpType.ANSWER
        SessionDescriptionType.Rollback -> RTCSdpType.ROLLBACK
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
