package com.shepeliev.webrtckmp

import WebRTC.RTCSdpType
import WebRTC.RTCSessionDescription

actual class SessionDescription internal constructor(val native: RTCSessionDescription) {

    actual constructor(type: SessionDescriptionType, description: String) : this(
        RTCSessionDescription(type.asNative(), description)
    )

    actual val type: SessionDescriptionType = rtcSdpTypeAsCommon(native.type)
    actual val description: String = native.description!!
}

private fun rtcSdpTypeAsCommon(type: RTCSdpType): SessionDescriptionType {
    return when (type) {
        RTCSdpType.RTCSdpTypeOffer -> SessionDescriptionType.Offer
        RTCSdpType.RTCSdpTypePrAnswer -> SessionDescriptionType.Pranswer
        RTCSdpType.RTCSdpTypeAnswer -> SessionDescriptionType.Answer
        RTCSdpType.RTCSdpTypeRollback -> SessionDescriptionType.Rollback
    }
}

private fun SessionDescriptionType.asNative(): RTCSdpType {
    return when (this) {
        SessionDescriptionType.Offer -> RTCSdpType.RTCSdpTypeOffer
        SessionDescriptionType.Pranswer -> RTCSdpType.RTCSdpTypePrAnswer
        SessionDescriptionType.Answer -> RTCSdpType.RTCSdpTypeAnswer
        SessionDescriptionType.Rollback -> RTCSdpType.RTCSdpTypeRollback
    }
}
