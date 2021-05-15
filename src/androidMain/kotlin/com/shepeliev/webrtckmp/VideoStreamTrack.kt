package com.shepeliev.webrtckmp

import org.webrtc.VideoSink
import org.webrtc.VideoTrack as AndroidVideoTrack

actual class VideoStreamTrack internal constructor(
    private val videoTrack: AndroidVideoTrack,
    remote: Boolean,
) : MediaStreamTrack(videoTrack, remote) {

    fun addSink(sink: VideoSink) {
        videoTrack.addSink(sink)
    }

    fun removeSink(sink: VideoSink) {
        videoTrack.removeSink(sink)
    }
}
