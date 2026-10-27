@file:OptIn(ExperimentalForeignApi::class)

package com.shepeliev.webrtckmp

import kotlinx.cinterop.ExperimentalForeignApi
import swiftPMImport.com.shepeliev.webrtc.kmp.RTCAudioTrack

internal class RemoteAudioTrack(
    private val iosTrack: RTCAudioTrack,
) : MediaStreamTrackImpl(iosTrack),
    AudioTrack {
    override fun setVolume(volume: Double) {
        iosTrack.source.setVolume(volume)
    }
}
