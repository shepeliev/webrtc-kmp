package com.shepeliev.webrtckmp

actual class SessionDescription internal constructor(val js: RTCSessionDescription) {
    actual val type: SessionDescriptionType = js.type.toSessionDescriptionType()
    actual val sdp: String = js.sdp

    private fun String.toSessionDescriptionType(): SessionDescriptionType = when (this) {
        "answer" -> SessionDescriptionType.Answer
        "offer" -> SessionDescriptionType.Offer
        "pranswer" -> SessionDescriptionType.Pranswer
        "rollback" -> SessionDescriptionType.Rollback
        else -> throw IllegalArgumentException("Illegal session description type: $this")
    }
}

