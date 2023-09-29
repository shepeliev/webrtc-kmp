package com.shepeliev.webrtckmp

import WebRTC.RTCVideoRendererProtocol
import WebRTC.RTCVideoTrack

internal abstract class RenderedVideoTrack(
    ios: RTCVideoTrack
) : MediaStreamTrackImpl(ios), VideoTrack {
    override fun addRenderer(renderer: RTCVideoRendererProtocol) {
        (native as RTCVideoTrack).addRenderer(renderer)
    }

    override fun removeRenderer(renderer: RTCVideoRendererProtocol) {
        (native as RTCVideoTrack).removeRenderer(renderer)
    }
}
