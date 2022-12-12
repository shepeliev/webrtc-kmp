package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.media.audio.AudioTrack
import dev.onvoid.webrtc.media.audio.AudioTrackSource

actual class AudioStreamTrack internal constructor(
    jvm: AudioTrack,
    private val audioSource: AudioTrackSource? = null,
) : MediaStreamTrack(jvm) {

    override fun onSetEnabled(enabled: Boolean) {
        // ignore
    }

    override fun onStop() {
        audioSource?.dispose() // TODO("Find how to dispose this source")
    }
}
