package com.shepeliev.webrtckmp

import org.webrtc.AudioSource
import org.webrtc.AudioTrack

actual class AudioStreamTrack internal constructor(
    android: AudioTrack,
    internal val audioSource: AudioSource? = null,
) : MediaStreamTrack(android) {

    override fun onSetEnabled(enabled: Boolean) {
        // ignore
    }

    override fun onStop() {
        audioSource?.dispose()
    }
}
