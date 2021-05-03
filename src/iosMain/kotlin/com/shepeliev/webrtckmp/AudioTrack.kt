package com.shepeliev.webrtckmp

import WebRTC.RTCAudioTrack

actual class AudioTrack internal constructor(override val native: RTCAudioTrack) :
    BaseMediaStreamTrack(), MediaStreamTrack {

    actual fun setVolume(volume: Double) {
        native.source().setVolume(volume)
    }
}
