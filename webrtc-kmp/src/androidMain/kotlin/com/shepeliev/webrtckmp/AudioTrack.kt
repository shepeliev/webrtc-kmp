package com.shepeliev.webrtckmp

import org.webrtc.AudioTrackSink

actual interface AudioTrack : MediaStreamTrack {
    fun addSink(sink: AudioTrackSink)
    fun removeSink(sink: AudioTrackSink)
}
