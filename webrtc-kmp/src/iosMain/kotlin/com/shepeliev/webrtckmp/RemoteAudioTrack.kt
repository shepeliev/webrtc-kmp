@file:OptIn(ExperimentalForeignApi::class)

package com.shepeliev.webrtckmp

import WebRTC.RTCAudioTrack
import kotlinx.cinterop.ExperimentalForeignApi

internal class RemoteAudioTrack(
    ios: RTCAudioTrack,
) : MediaStreamTrackImpl(ios),
    AudioTrack {
    override fun setVolume(volume: Double) {
        (ios as RTCAudioTrack).source().setVolume(volume)
    }
}
