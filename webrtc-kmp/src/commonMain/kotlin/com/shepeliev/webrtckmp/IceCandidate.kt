package com.shepeliev.webrtckmp

public expect class IceCandidate(
    sdpMid: String,
    sdpMLineIndex: Int,
    candidate: String,
) {
    public val sdpMid: String
    public val sdpMLineIndex: Int
    public val candidate: String

    override fun toString(): String
}
