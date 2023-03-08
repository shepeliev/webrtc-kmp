package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.media.audio.AudioTrack

actual class AudioStreamTrack internal constructor(
    private val audioTrack: AudioTrack,
) : MediaStreamTrack(native = audioTrack) {

    override fun onSetEnabled(enabled: Boolean) {
        // ignore
    }

    override fun onStop() {
        audioTrack.dispose()
    }
}
