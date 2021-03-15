package com.shepeliev.webrtckmm

import org.webrtc.VideoSink

class VideoSinkAdapter(val native: VideoSink): VideoRenderer {
    override fun onFrame(frame: VideoFrame) = native.onFrame(frame.native)
}
