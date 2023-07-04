package com.shepeliev.webrtckmp

import org.webrtc.VideoSink
import org.webrtc.VideoTrack

internal abstract class RenderedVideoStreamTrack(
    android: VideoTrack
) : MediaStreamTrackImpl(android), VideoStreamTrack {
    override fun addSink(sink: VideoSink) {
        android as VideoTrack
        android.addSink(sink)
    }

    override fun removeSink(sink: VideoSink) {
        android as VideoTrack
        android.removeSink(sink)
    }
}
