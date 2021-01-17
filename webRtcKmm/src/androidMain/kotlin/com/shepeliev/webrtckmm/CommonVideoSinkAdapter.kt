package com.shepeliev.webrtckmm

import org.webrtc.VideoSink as NativeVideoSink

internal class CommonVideoSinkAdapter(private val sink: VideoSink): NativeVideoSink {
    override fun onFrame(frame: org.webrtc.VideoFrame) {
        sink.onFrame(VideoFrame(frame))
    }
}
