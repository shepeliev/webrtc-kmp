package com.shepeliev.webrtckmm

import cocoapods.GoogleWebRTC.RTCSdpType
import cocoapods.GoogleWebRTC.RTCSessionDescription

actual class SessionDescription internal constructor(val native: RTCSessionDescription) {

    actual constructor(type: SessionDescriptionType, description: String) : this(
        RTCSessionDescription(type.asNative(), description)
    )

    actual val type: SessionDescriptionType = rtcSdpTypeAsCommon(native.type)
    actual val description: String = native.description!!
}

actual fun sessionDescriptionTypeFromCanonicalForm(canonical: String): SessionDescriptionType {
//    return NativeSessionDescription.Type.fromCanonicalForm(canonical).asCommon()
    TODO()
}

private fun rtcSdpTypeAsCommon(type: RTCSdpType): SessionDescriptionType {
    return when (type) {
        RTCSdpType.RTCSdpTypeOffer -> SessionDescriptionType.Offer
        RTCSdpType.RTCSdpTypePrAnswer -> SessionDescriptionType.Pranswer
        RTCSdpType.RTCSdpTypeAnswer -> SessionDescriptionType.Answer
    }
}

private fun SessionDescriptionType.asNative(): RTCSdpType {
    return when (this) {
        SessionDescriptionType.Offer -> RTCSdpType.RTCSdpTypeOffer
        SessionDescriptionType.Pranswer -> RTCSdpType.RTCSdpTypePrAnswer
        SessionDescriptionType.Answer -> RTCSdpType.RTCSdpTypeAnswer
    }
}
