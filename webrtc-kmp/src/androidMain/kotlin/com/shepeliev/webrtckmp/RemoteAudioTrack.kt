package com.shepeliev.webrtckmp

import org.webrtc.AudioTrackSink
import org.webrtc.AudioTrack as AndroidAudioTrack

internal class RemoteAudioTrack(
    native: AndroidAudioTrack
) : MediaStreamTrackImpl(native), AudioTrack {
    override fun addSink(sink: AudioTrackSink) {
        (native as AndroidAudioTrack).addSink(sink)
    }

    override fun removeSink(sink: AudioTrackSink) {
        (native as AndroidAudioTrack).removeSink(sink)
    }
}
