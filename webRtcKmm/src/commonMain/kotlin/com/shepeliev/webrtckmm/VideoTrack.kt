package com.shepeliev.webrtckmm

expect class VideoTrack : MediaStreamTrack {
    fun addSink(sink: VideoSink)
    fun removeSink(sink: VideoSink)
}
