package com.shepeliev.webrtckmp

import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.AudioTrackSink

internal class LocalAudioStreamTrack(
    android: AudioTrack,
    private val audioSource: AudioSource,
    override val constraints: MediaTrackConstraints,
) : MediaStreamTrackImpl(android), AudioStreamTrack {

    override fun addSink(sink: AudioTrackSink) {
        (native as AudioTrack).addSink(sink)
    }

    override fun removeSink(sink: AudioTrackSink) {
        (native as AudioTrack).removeSink(sink)
    }

    override fun onStop() {
        audioSource.dispose()
    }
}
