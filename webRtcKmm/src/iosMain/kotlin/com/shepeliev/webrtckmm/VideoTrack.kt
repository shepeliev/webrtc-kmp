package com.shepeliev.webrtckmm

import cocoapods.GoogleWebRTC.RTCVideoTrack

actual class VideoTrack internal constructor(override val native: RTCVideoTrack):
    BaseMediaStreamTrack(), MediaStreamTrack {

    actual fun addSink(renderer: VideoRenderer) {
        require(renderer is RTCVideoRendererProtocolAdapter) { "renderer must be instance of RTCVideoRendererProtocolAdapter" }
        native.addRenderer(renderer.native)
    }

    actual fun removeSink(renderer: VideoRenderer) {
        require(renderer is RTCVideoRendererProtocolAdapter) { "renderer must be instance of RTCVideoRendererProtocolAdapter" }
        native.removeRenderer(renderer.native)
    }
}
