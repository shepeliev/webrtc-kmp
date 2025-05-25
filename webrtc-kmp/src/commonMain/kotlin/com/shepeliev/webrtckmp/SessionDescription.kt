package com.shepeliev.webrtckmp

public data class SessionDescription(
    val type: SessionDescriptionType,
    val sdp: String,
)

public enum class SessionDescriptionType { Offer, Pranswer, Answer, Rollback }

public fun SessionDescriptionType.toCanonicalString(): String = name.lowercase()
