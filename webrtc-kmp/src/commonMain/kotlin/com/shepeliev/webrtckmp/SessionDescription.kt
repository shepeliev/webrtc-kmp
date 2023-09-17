@file:Suppress("unused")
package com.shepeliev.webrtckmp

data class SessionDescription(val type: SessionDescriptionType, val sdp: String)

enum class SessionDescriptionType {
    Offer, Pranswer, Answer, Rollback;

    companion object {
        fun fromCanonicalForm(canonicalForm: String): SessionDescriptionType {
            return when (canonicalForm) {
                "offer" -> Offer
                "pranswer" -> Pranswer
                "answer" -> Answer
                "rollback" -> Rollback
                else -> throw IllegalArgumentException("Unknown canonical form: $canonicalForm")
            }
        }
    }
}

fun SessionDescriptionType.canonicalForm(): String = name.lowercase()
