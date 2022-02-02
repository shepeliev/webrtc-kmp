package com.shepeliev.webrtckmp

import org.webrtc.AudioTrack

actual class AudioStreamTrack internal constructor(
    android: AudioTrack,
    private val onStop: () -> Unit = { },
) : MediaStreamTrack(android) {

    override fun stop() {
        onStop()
        super.stop()
    }
}
