package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.RTCSdpType
import dev.onvoid.webrtc.RTCSessionDescription

internal fun SessionDescription.asNative(): RTCSessionDescription {
    return RTCSessionDescription(type.asNative(), sdp)
}

private fun SessionDescriptionType.asNative(): RTCSdpType {
    return when (this) {
        SessionDescriptionType.Offer -> RTCSdpType.OFFER
        SessionDescriptionType.Pranswer -> RTCSdpType.PR_ANSWER
        SessionDescriptionType.Answer -> RTCSdpType.ANSWER
        SessionDescriptionType.Rollback -> RTCSdpType.ROLLBACK
    }
}

internal fun RTCSessionDescription.asCommon(): SessionDescription {
    return SessionDescription(sdpType.asCommon(), sdp)
}

private fun RTCSdpType.asCommon(): SessionDescriptionType {
    return when (this) {
        RTCSdpType.OFFER -> SessionDescriptionType.Offer
        RTCSdpType.PR_ANSWER -> SessionDescriptionType.Pranswer
        RTCSdpType.ANSWER -> SessionDescriptionType.Answer
        RTCSdpType.ROLLBACK -> SessionDescriptionType.Rollback
    }
}
