package com.shepeliev.webrtckmp

import WebRTC.RTCVideoRendererProtocol
import WebRTC.RTCVideoTrack

internal abstract class RenderedVideoStreamTrack(
    ios: RTCVideoTrack
) : MediaStreamTrackImpl(ios), VideoStreamTrack {
    override fun addRenderer(renderer: RTCVideoRendererProtocol) {
        native as RTCVideoTrack
        native.addRenderer(renderer)
    }

    override fun removeRenderer(renderer: RTCVideoRendererProtocol) {
        native as RTCVideoTrack
        native.removeRenderer(renderer)
    }
}
