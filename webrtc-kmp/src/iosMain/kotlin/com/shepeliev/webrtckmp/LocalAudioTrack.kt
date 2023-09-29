package com.shepeliev.webrtckmp

import WebRTC.RTCAudioRendererProtocol
import WebRTC.RTCAudioTrack

internal class LocalAudioTrack(
    native: RTCAudioTrack,
    override val constraints: MediaTrackConstraints,
) : MediaStreamTrackImpl(native), AudioTrack {
    override fun addRenderer(renderer: RTCAudioRendererProtocol) {
        (native as RTCAudioTrack).addRenderer(renderer)
    }

    override fun removeRenderer(renderer: RTCAudioRendererProtocol) {
        (native as RTCAudioTrack).removeRenderer(renderer)
    }
}
