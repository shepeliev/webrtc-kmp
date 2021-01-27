package com.shepeliev.webrtckmm

data class CandidatePairChangeEvent(
    val local: IceCandidate,
    val remote: IceCandidate,
    val lastDataReceivedMs: Int,
    val reason: String,
    val estimatedDisconnectedTimeMs: Int,
)
