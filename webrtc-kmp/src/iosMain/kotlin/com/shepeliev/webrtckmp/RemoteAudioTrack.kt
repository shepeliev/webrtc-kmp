@file:OptIn(ExperimentalForeignApi::class)

package com.shepeliev.webrtckmp

import WebRTC.RTCAudioTrack
import kotlinx.cinterop.ExperimentalForeignApi

internal class RemoteAudioTrack(
    private val iosTrack: RTCAudioTrack,
) : MediaStreamTrackImpl(iosTrack),
    AudioTrack {
    override fun setVolume(volume: Double) {
        iosTrack.source.setVolume(volume)
    }
}
