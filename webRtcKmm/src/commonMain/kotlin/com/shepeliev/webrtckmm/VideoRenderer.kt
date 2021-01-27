package com.shepeliev.webrtckmm

interface VideoRenderer {
    fun onFrame(frame: VideoFrame)
    fun setSize(size: Size) {}
}

data class Size(val width: Double, val height: Double)
