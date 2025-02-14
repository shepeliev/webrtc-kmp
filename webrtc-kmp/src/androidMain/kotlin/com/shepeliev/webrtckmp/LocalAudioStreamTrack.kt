package com.shepeliev.webrtckmp

import org.webrtc.AudioSource
import org.webrtc.AudioTrack

internal class LocalAudioStreamTrack(
    android: AudioTrack,
    private val audioSource: AudioSource,
    override val constraints: MediaTrackConstraints,
) : MediaStreamTrackImpl(android), AudioStreamTrack {

    override fun onStop() {
        audioSource.dispose()
    }

    override fun setVolume(volume: Double) {
        (android as AudioTrack).setVolume(volume)
    }
}
