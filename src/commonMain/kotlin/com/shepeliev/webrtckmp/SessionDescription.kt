package com.shepeliev.webrtckmp

expect class SessionDescription(type: SessionDescriptionType, description: String) {
    val type: SessionDescriptionType
    val description: String
}

enum class SessionDescriptionType { Offer, Pranswer, Answer, Rollback }

expect fun sessionDescriptionTypeFromCanonicalForm(canonical: String): SessionDescriptionType
