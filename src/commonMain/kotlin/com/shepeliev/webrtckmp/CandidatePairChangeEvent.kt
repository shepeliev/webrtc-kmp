package com.shepeliev.webrtckmp

data class CandidatePairChangeEvent(
    val local: IceCandidate,
    val remote: IceCandidate,
    val lastDataReceivedMs: Int,
    val reason: String,
    val estimatedDisconnectedTimeMs: Int,
)
