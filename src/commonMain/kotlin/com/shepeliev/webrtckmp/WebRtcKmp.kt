package com.shepeliev.webrtckmp

expect object WebRtcKmp {
    internal val peerConnectionFactory: PeerConnectionFactory
}

internal const val NOT_INITIALIZED_ERROR_MESSAGE = "WebRTC KMM not initialized."
