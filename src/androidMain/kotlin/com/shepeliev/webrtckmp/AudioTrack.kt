package com.shepeliev.webrtckmp

import org.webrtc.AudioTrack as NativeAudioTrack

actual class AudioTrack internal constructor(override val native: NativeAudioTrack) :
    BaseMediaStreamTrack(), MediaStreamTrack {

    actual fun setVolume(volume: Double) {
        native.setVolume(volume)
    }
}

internal fun NativeAudioTrack.asCommon() = AudioTrack(this)
