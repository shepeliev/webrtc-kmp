package com.shepeliev.webrtckmp

import WebRTC.RTCSdpType
import WebRTC.RTCSessionDescription

actual class SessionDescription internal constructor(val ios: RTCSessionDescription) {
    actual val type: SessionDescriptionType = rtcSdpTypeAsCommon(ios.type)
    actual val sdp: String = ios.description!!
}

private fun rtcSdpTypeAsCommon(type: RTCSdpType): SessionDescriptionType {
    return when (type) {
        RTCSdpType.RTCSdpTypeOffer -> SessionDescriptionType.Offer
        RTCSdpType.RTCSdpTypePrAnswer -> SessionDescriptionType.Pranswer
        RTCSdpType.RTCSdpTypeAnswer -> SessionDescriptionType.Answer
        RTCSdpType.RTCSdpTypeRollback -> SessionDescriptionType.Rollback
    }
}
