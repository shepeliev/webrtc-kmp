package com.shepeliev.webrtckmm

import kotlin.jvm.JvmOverloads

data class IceCandidate @JvmOverloads constructor(
    val sdpMid: String,
    val sdpMLineIndex: Int,
    val sdp: String,
    val serverUrl: String = "",
    val adapterType: AdapterType = AdapterType.Unknown
)
