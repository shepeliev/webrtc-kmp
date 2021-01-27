package com.shepeliev.webrtckmm

import org.webrtc.VideoSink as NativeVideoSink

internal class CommonVideoSinkAdapter(private val renderer: VideoRenderer): NativeVideoSink {
    override fun onFrame(frame: org.webrtc.VideoFrame) {
        renderer.onFrame(VideoFrame(frame))
    }
}
