package com.shepeliev.webrtckmp

import WebRTC.RTCVideoRendererProtocol
import WebRTC.RTCVideoTrack

internal abstract class RenderedVideoStreamTrack(
    ios: RTCVideoTrack
) : MediaStreamTrackImpl(ios), VideoStreamTrack {
    override fun addRenderer(renderer: RTCVideoRendererProtocol) {
        ios as RTCVideoTrack
        ios.addRenderer(renderer)
    }

    override fun removeRenderer(renderer: RTCVideoRendererProtocol) {
        ios as RTCVideoTrack
        ios.removeRenderer(renderer)
    }
}
