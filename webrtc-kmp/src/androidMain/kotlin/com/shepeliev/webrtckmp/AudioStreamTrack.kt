package com.shepeliev.webrtckmp

import org.webrtc.AudioTrack

actual class AudioStreamTrack internal constructor(
    android: AudioTrack,
    private val onTrackStopped: () -> Unit = { },
) : MediaStreamTrack(android) {

    override fun onSetEnabled(enabled: Boolean) {
        // ignore
    }

    override fun onStop() {
        onTrackStopped()
    }
}
