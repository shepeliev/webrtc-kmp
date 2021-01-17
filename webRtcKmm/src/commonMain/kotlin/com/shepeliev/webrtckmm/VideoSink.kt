package com.shepeliev.webrtckmm

interface VideoSink {
    fun onFrame(frame: VideoFrame)
}
