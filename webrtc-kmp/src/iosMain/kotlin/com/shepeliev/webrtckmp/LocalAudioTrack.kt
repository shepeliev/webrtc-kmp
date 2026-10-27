@file:OptIn(ExperimentalForeignApi::class)

package com.shepeliev.webrtckmp

import kotlinx.cinterop.ExperimentalForeignApi
import swiftPMImport.com.shepeliev.webrtc.kmp.RTCAudioTrack

internal class LocalAudioTrack(
    private val iosTrack: RTCAudioTrack,
    override val constraints: MediaTrackConstraints,
) : MediaStreamTrackImpl(iosTrack),
    AudioTrack {
    override fun setVolume(volume: Double) {
        iosTrack.source.setVolume(volume)
    }
}
