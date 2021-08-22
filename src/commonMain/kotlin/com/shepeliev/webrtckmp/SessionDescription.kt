package com.shepeliev.webrtckmp

expect class SessionDescription {
    val type: SessionDescriptionType
    val sdp: String

    companion object {
        fun fromSdp(type: SessionDescriptionType, description: String): SessionDescription
    }
}

enum class SessionDescriptionType { Offer, Pranswer, Answer, Rollback }
