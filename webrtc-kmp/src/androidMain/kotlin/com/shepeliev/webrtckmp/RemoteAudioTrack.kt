package com.shepeliev.webrtckmp

import org.webrtc.AudioTrack as AndroidAudioTrack

internal class RemoteAudioTrack(
    private val androidTrack: AndroidAudioTrack,
) : MediaStreamTrackImpl(androidTrack),
    AudioTrack {
    override fun setVolume(volume: Double) {
        androidTrack.setVolume(volume)
    }
}
