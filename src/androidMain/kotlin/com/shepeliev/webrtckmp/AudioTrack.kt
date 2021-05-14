package com.shepeliev.webrtckmp

import org.webrtc.AudioTrack as AndroidAudioTrack

actual class AudioTrack internal constructor(
    override val native: AndroidAudioTrack,
) : BaseMediaStreamTrack(), MediaStreamTrack {

    actual fun setVolume(volume: Double) {
        native.setVolume(volume)
    }
}
