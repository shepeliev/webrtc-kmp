package com.shepeliev.webrtckmp

import org.webrtc.AudioTrack

actual class AudioStreamTrack internal constructor(
    private val audioTrack: AudioTrack,
    remote: Boolean,
) : MediaStreamTrack(audioTrack, remote) {

    fun setVolume(volume: Double) {
        audioTrack.setVolume(volume)
    }
}
