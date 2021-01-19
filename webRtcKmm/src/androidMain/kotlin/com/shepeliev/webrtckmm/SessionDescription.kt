package com.shepeliev.webrtckmm

import org.webrtc.SessionDescription as NativeSessionDescription

actual class SessionDescription internal constructor(val native: NativeSessionDescription) {

    actual constructor(type: SessionDescriptionType, description: String) : this(
        NativeSessionDescription(type.toNative(), description)
    )

    actual val type: SessionDescriptionType = native.type.toCommon()
    actual val description: String = native.description
}

actual fun sessionDescriptionTypeFromCanonicalForm(canonical: String): SessionDescriptionType {
    return NativeSessionDescription.Type.fromCanonicalForm(canonical).toCommon()
}

fun NativeSessionDescription.Type.toCommon(): SessionDescriptionType {
    return when (this) {
        NativeSessionDescription.Type.OFFER -> SessionDescriptionType.Offer
        NativeSessionDescription.Type.PRANSWER -> SessionDescriptionType.Pranswer
        NativeSessionDescription.Type.ANSWER -> SessionDescriptionType.Answer
    }
}

fun SessionDescriptionType.toNative(): NativeSessionDescription.Type {
    return when (this) {
        SessionDescriptionType.Offer -> NativeSessionDescription.Type.OFFER
        SessionDescriptionType.Pranswer -> NativeSessionDescription.Type.PRANSWER
        SessionDescriptionType.Answer -> NativeSessionDescription.Type.ANSWER
    }
}

fun NativeSessionDescription.toCommon(): SessionDescription = SessionDescription(this)
