package com.shepeliev.webrtckmp

import org.webrtc.SessionDescription as NativeSessionDescription

actual class SessionDescription internal constructor(val native: NativeSessionDescription) {

    actual constructor(type: SessionDescriptionType, description: String) : this(
        NativeSessionDescription(type.asNative(), description)
    )

    actual val type: SessionDescriptionType = native.type.asCommon()
    actual val description: String = native.description
}

fun NativeSessionDescription.Type.asCommon(): SessionDescriptionType {
    return when (this) {
        NativeSessionDescription.Type.OFFER -> SessionDescriptionType.Offer
        NativeSessionDescription.Type.PRANSWER -> SessionDescriptionType.Pranswer
        NativeSessionDescription.Type.ANSWER -> SessionDescriptionType.Answer
        NativeSessionDescription.Type.ROLLBACK -> SessionDescriptionType.Rollback
    }
}

fun SessionDescriptionType.asNative(): NativeSessionDescription.Type {
    return when (this) {
        SessionDescriptionType.Offer -> NativeSessionDescription.Type.OFFER
        SessionDescriptionType.Pranswer -> NativeSessionDescription.Type.PRANSWER
        SessionDescriptionType.Answer -> NativeSessionDescription.Type.ANSWER
        SessionDescriptionType.Rollback -> NativeSessionDescription.Type.ROLLBACK
    }
}

fun NativeSessionDescription.asCommon(): SessionDescription = SessionDescription(this)
