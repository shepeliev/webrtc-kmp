package com.shepeliev.webrtckmp

expect class IceCandidate(sdpMid: String, sdpMLineIndex: Int, candidate: String) {
    val sdpMid: String
    val sdpMLineIndex: Int
    val candidate: String

    override fun toString(): String
}
