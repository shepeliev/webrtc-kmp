package com.shepeliev.webrtckmm

expect class VideoTrack : MediaStreamTrack {
    fun addSink(renderer: VideoRenderer)
    fun removeSink(renderer: VideoRenderer)
}
