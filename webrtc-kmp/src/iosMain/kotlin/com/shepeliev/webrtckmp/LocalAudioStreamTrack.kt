package com.shepeliev.webrtckmp

import WebRTC.RTCAudioRendererProtocol
import WebRTC.RTCAudioTrack

internal class LocalAudioStreamTrack(
    native: RTCAudioTrack,
    override val constraints: MediaTrackConstraints,
) : MediaStreamTrackImpl(native), AudioStreamTrack {
    fun addRenderer(renderer: RTCAudioRendererProtocol) {
        (native as RTCAudioTrack).addRenderer(renderer)
    }

    fun removeRenderer(renderer: RTCAudioRendererProtocol) {
        (native as RTCAudioTrack).removeRenderer(renderer)
    }
}
