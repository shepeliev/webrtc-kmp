package com.shepeliev.webrtckmp

import WebRTC.RTCAudioRendererProtocol

actual interface AudioTrack : MediaStreamTrack {
    fun addRenderer(renderer: RTCAudioRendererProtocol)
    fun removeRenderer(renderer: RTCAudioRendererProtocol)
}
