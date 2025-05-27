@file:OptIn(ExperimentalForeignApi::class)

package com.shepeliev.webrtckmp

import WebRTC.RTCAudioTrack
import kotlinx.cinterop.ExperimentalForeignApi

internal class LocalAudioTrack(
    private val iosTrack: RTCAudioTrack,
    override val constraints: MediaTrackConstraints,
) : MediaStreamTrackImpl(iosTrack),
    AudioTrack {
    override fun setVolume(volume: Double) {
        iosTrack.source.setVolume(volume)
    }
}
