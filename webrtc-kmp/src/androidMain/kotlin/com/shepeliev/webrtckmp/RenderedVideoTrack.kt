package com.shepeliev.webrtckmp

import org.webrtc.VideoSink
import org.webrtc.VideoTrack as AndroidVideoTrack

internal abstract class RenderedVideoTrack(
    private val androidTrack: AndroidVideoTrack,
) : MediaStreamTrackImpl(androidTrack),
    VideoTrack {
    override fun addSink(sink: VideoSink) {
        androidTrack.addSink(sink)
    }

    override fun removeSink(sink: VideoSink) {
        androidTrack.removeSink(sink)
    }
}
