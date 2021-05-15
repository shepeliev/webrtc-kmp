package com.shepeliev.webrtckmp

import WebRTC.RTCAudioTrack

actual class AudioStreamTrack internal constructor(
    private val videoTrack: RTCAudioTrack,
    remote: Boolean,
) : MediaStreamTrack(videoTrack, remote) {

    fun setVolume(volume: Double) {
        videoTrack.source().setVolume(volume)
    }
}
