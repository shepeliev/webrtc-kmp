package com.shepeliev.webrtckmm

expect class SessionDescription {
    val type: SessionDescriptionType
    val description: String
}

enum class SessionDescriptionType { Offer, Pranswer, Answer }
