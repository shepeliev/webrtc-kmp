package com.shepeliev.webrtckmp

data class SessionDescription(val type: SessionDescriptionType, val sdp: String)

enum class SessionDescriptionType { Offer, Pranswer, Answer, Rollback }
