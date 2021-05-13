package com.shepeliev.webrtckmp

import kotlinx.coroutines.CoroutineScope

expect object WebRtcKmp {
    val mainScope: CoroutineScope
    internal val peerConnectionFactory: PeerConnectionFactory
}

internal const val NOT_INITIALIZED_ERROR_MESSAGE = "WebRTC KMM not initialized."
