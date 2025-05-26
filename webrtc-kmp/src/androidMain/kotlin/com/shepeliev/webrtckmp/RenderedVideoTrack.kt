package com.shepeliev.webrtckmp

import org.webrtc.VideoSink
import org.webrtc.VideoTrack as AndroidVideoTrack

internal abstract class RenderedVideoTrack(
    android: AndroidVideoTrack,
) : MediaStreamTrackImpl(android),
    VideoTrack {
    override fun addSink(sink: VideoSink) {
        android as VideoTrack
        android.addSink(sink)
    }

    override fun removeSink(sink: VideoSink) {
        android as VideoTrack
        android.removeSink(sink)
    }
}
