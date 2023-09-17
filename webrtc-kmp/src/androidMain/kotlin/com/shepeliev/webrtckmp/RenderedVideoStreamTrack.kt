package com.shepeliev.webrtckmp

import org.webrtc.VideoSink
import org.webrtc.VideoTrack

internal abstract class RenderedVideoStreamTrack(
    android: VideoTrack
) : MediaStreamTrackImpl(android), VideoStreamTrack {
    override fun addSink(sink: VideoSink) {
        native as VideoTrack
        native.addSink(sink)
    }

    override fun removeSink(sink: VideoSink) {
        native as VideoTrack
        native.removeSink(sink)
    }
}
