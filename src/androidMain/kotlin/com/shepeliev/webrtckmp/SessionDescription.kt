package com.shepeliev.webrtckmp

import org.webrtc.SessionDescription as NativeSessionDescription

actual class SessionDescription internal constructor(val native: NativeSessionDescription) {

    actual constructor(type: SessionDescriptionType, description: String) : this(
        NativeSessionDescription(type.asNative(), description)
    )

    actual val type: SessionDescriptionType = native.type.asCommon()
    actual val description: String = native.description
}

actual fun sessionDescriptionTypeFromCanonicalForm(canonical: String): SessionDescriptionType {
    return NativeSessionDescription.Type.fromCanonicalForm(canonical).asCommon()
}

fun NativeSessionDescription.Type.asCommon(): SessionDescriptionType {
    return when (this) {
        NativeSessionDescription.Type.OFFER -> SessionDescriptionType.Offer
        NativeSessionDescription.Type.PRANSWER -> SessionDescriptionType.Pranswer
        NativeSessionDescription.Type.ANSWER -> SessionDescriptionType.Answer
    }
}

fun SessionDescriptionType.asNative(): NativeSessionDescription.Type {
    return when (this) {
        SessionDescriptionType.Offer -> NativeSessionDescription.Type.OFFER
        SessionDescriptionType.Pranswer -> NativeSessionDescription.Type.PRANSWER
        SessionDescriptionType.Answer -> NativeSessionDescription.Type.ANSWER
        SessionDescriptionType.Rollback -> error("Not implemented")
    }
}

fun NativeSessionDescription.asCommon(): SessionDescription = SessionDescription(this)
