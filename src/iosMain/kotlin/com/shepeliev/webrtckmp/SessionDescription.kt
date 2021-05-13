package com.shepeliev.webrtckmp

import WebRTC.RTCSdpType
import WebRTC.RTCSessionDescription

actual class SessionDescription internal constructor(val native: RTCSessionDescription) {
    actual val type: SessionDescriptionType = rtcSdpTypeAsCommon(native.type)
    actual val sdp: String = native.description!!
}

private fun rtcSdpTypeAsCommon(type: RTCSdpType): SessionDescriptionType {
    return when (type) {
        RTCSdpType.RTCSdpTypeOffer -> SessionDescriptionType.Offer
        RTCSdpType.RTCSdpTypePrAnswer -> SessionDescriptionType.Pranswer
        RTCSdpType.RTCSdpTypeAnswer -> SessionDescriptionType.Answer
        RTCSdpType.RTCSdpTypeRollback -> SessionDescriptionType.Rollback
    }
}
