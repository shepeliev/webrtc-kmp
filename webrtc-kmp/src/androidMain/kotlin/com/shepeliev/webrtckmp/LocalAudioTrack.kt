package com.shepeliev.webrtckmp

import org.webrtc.AudioSource
import org.webrtc.AudioTrackSink
import org.webrtc.AudioTrack as AndroidAudioTrack

internal class LocalAudioTrack(
    native: AndroidAudioTrack,
    private val audioSource: AudioSource,
    override val constraints: MediaTrackConstraints,
) : MediaStreamTrackImpl(native), AudioTrack {

    override fun addSink(sink: AudioTrackSink) {
        (native as AndroidAudioTrack).addSink(sink)
    }

    override fun removeSink(sink: AudioTrackSink) {
        (native as AndroidAudioTrack).removeSink(sink)
    }

    override fun onStop() {
        audioSource.dispose()
    }
}
