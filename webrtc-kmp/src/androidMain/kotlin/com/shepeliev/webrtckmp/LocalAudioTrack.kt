package com.shepeliev.webrtckmp

import org.webrtc.AudioSource
import org.webrtc.AudioTrack as AndroidAudioTrack

internal class LocalAudioTrack(
    android: AndroidAudioTrack,
    private val audioSource: AudioSource,
    override val constraints: MediaTrackConstraints,
) : MediaStreamTrackImpl(android),
    AudioTrack {
    override fun onStop() {
        audioSource.dispose()
    }

    override fun setVolume(volume: Double) {
        (android as AudioTrack).setVolume(volume)
    }
}
