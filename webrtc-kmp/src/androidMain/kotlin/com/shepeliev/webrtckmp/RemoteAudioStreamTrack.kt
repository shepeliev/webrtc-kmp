package com.shepeliev.webrtckmp

import org.webrtc.AudioTrack
import org.webrtc.AudioTrackSink

internal class RemoteAudioStreamTrack(
    android: AudioTrack
) : MediaStreamTrackImpl(android), AudioStreamTrack {
    override fun addSink(sink: AudioTrackSink) {
        (native as AudioTrack).addSink(sink)
    }

    override fun removeSink(sink: AudioTrackSink) {
        (native as AudioTrack).removeSink(sink)
    }
}
