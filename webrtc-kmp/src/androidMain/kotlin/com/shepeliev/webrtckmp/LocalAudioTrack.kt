package com.shepeliev.webrtckmp

import org.webrtc.AudioSource
import org.webrtc.AudioTrack as AndroidAudioTrack

internal class LocalAudioTrack(
    private val androidTrack: AndroidAudioTrack,
    private val audioSource: AudioSource,
    override val constraints: MediaTrackConstraints,
) : MediaStreamTrackImpl(androidTrack),
    AudioTrack {
    override fun onStop() {
        audioSource.dispose()
    }

    override fun setVolume(volume: Double) {
        androidTrack.setVolume(volume)
    }
}
