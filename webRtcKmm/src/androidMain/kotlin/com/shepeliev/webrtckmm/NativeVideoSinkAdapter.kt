package com.shepeliev.webrtckmm

import org.webrtc.VideoSink as NativeVideoSink

class NativeVideoSinkAdapter(private val sink: NativeVideoSink): VideoSink {
    override fun onFrame(frame: VideoFrame) {
        sink.onFrame(frame.native)
    }
}
