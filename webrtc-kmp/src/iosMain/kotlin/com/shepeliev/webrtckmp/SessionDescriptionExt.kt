package com.shepeliev.webrtckmp

import WebRTC.RTCSdpType
import WebRTC.RTCSessionDescription

internal fun SessionDescription.asIos(): RTCSessionDescription {
    return RTCSessionDescription(type.asIos(), sdp)
}

private fun SessionDescriptionType.asIos(): RTCSdpType {
    return when (this) {
        SessionDescriptionType.Offer -> RTCSdpType.RTCSdpTypeOffer
        SessionDescriptionType.Pranswer -> RTCSdpType.RTCSdpTypePrAnswer
        SessionDescriptionType.Answer -> RTCSdpType.RTCSdpTypeAnswer
        SessionDescriptionType.Rollback -> RTCSdpType.RTCSdpTypeRollback
    }
}

internal fun RTCSessionDescription.asCommon(): SessionDescription {
    return SessionDescription(rtcSdpTypeAsCommon(type), sdp)
}

private fun rtcSdpTypeAsCommon(type: RTCSdpType): SessionDescriptionType {
    return when (type) {
        RTCSdpType.RTCSdpTypeOffer -> SessionDescriptionType.Offer
        RTCSdpType.RTCSdpTypePrAnswer -> SessionDescriptionType.Pranswer
        RTCSdpType.RTCSdpTypeAnswer -> SessionDescriptionType.Answer
        RTCSdpType.RTCSdpTypeRollback -> SessionDescriptionType.Rollback
        else -> error("Unknown RTCSdpType: $type")
    }
}
