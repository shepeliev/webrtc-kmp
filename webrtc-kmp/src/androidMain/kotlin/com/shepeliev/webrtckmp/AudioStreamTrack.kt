package com.shepeliev.webrtckmp

import org.webrtc.AudioTrackSink

actual interface AudioStreamTrack : MediaStreamTrack {
    fun addSink(sink: AudioTrackSink)
    fun removeSink(sink: AudioTrackSink)
}
