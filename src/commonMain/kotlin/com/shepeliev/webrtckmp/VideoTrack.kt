package com.shepeliev.webrtckmp

expect class VideoTrack : MediaStreamTrack {
    fun addSink(renderer: VideoRenderer)
    fun removeSink(renderer: VideoRenderer)
}
