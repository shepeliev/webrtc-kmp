package com.shepeliev.webrtckmp

expect class IceCandidate {
    val sdpMid: String
    val sdpMLineIndex: Int
    val candidate: String

    override fun toString(): String
}
