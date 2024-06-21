package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.media.video.VideoTrack
import dev.onvoid.webrtc.media.video.VideoTrackSink

internal abstract class RenderedVideoStreamTrack(
    native: VideoTrack
) : MediaStreamTrackImpl(native), VideoStreamTrack {
    override fun addSink(sink: VideoTrackSink) {
        native as VideoTrack
        native.addSink(sink)
    }

    override fun removeSink(sink: VideoTrackSink) {
        native as VideoTrack
        native.removeSink(sink)
    }
}
