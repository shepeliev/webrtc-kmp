package com.shepeliev.webrtckmp

import org.webrtc.VideoSink
import org.webrtc.VideoTrack as AndroidVideoTrack

internal abstract class RenderedVideoTrack(
    native: AndroidVideoTrack
) : MediaStreamTrackImpl(native), VideoTrack {
    override fun addSink(sink: VideoSink) {
        (native as AndroidVideoTrack).addSink(sink)
    }

    override fun removeSink(sink: VideoSink) {
        (native as AndroidVideoTrack).removeSink(sink)
    }
}
