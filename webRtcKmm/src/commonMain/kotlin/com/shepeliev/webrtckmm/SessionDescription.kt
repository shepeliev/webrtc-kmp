package com.shepeliev.webrtckmm

expect class SessionDescription(type: SessionDescriptionType, description: String) {
    val type: SessionDescriptionType
    val description: String
}

enum class SessionDescriptionType { Offer, Pranswer, Answer }

expect fun sessionDescriptionTypeFromCanonicalForm(canonical: String): SessionDescriptionType
