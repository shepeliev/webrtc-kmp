package com.shepeliev.webrtckmp

import org.webrtc.VideoTrack as NativeVideoTrack

actual class VideoTrack internal constructor(override val native: NativeVideoTrack) :
    BaseMediaStreamTrack(), MediaStreamTrack {

    actual fun addSink(renderer: VideoRenderer) {
        require(renderer is VideoSinkAdapter) { "renderer must be instance of VideoSinkAdapter" }
        native.addSink(renderer.native)
    }

    actual fun removeSink(renderer: VideoRenderer) {
        require(renderer is VideoSinkAdapter) { "renderer must be instance of VideoSinkAdapter" }
        native.removeSink(renderer.native)
    }
}
