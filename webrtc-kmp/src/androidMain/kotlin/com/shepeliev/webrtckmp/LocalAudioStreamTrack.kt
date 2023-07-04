package com.shepeliev.webrtckmp

import org.webrtc.AudioSource
import org.webrtc.AudioTrack

internal class LocalAudioStreamTrack(
    android: AudioTrack,
    private val audioSource: AudioSource,
) : MediaStreamTrackImpl(android), AudioStreamTrack {

    override fun onStop() {
        audioSource.dispose()
    }
}
