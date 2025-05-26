package com.shepeliev.webrtckmp

import org.webrtc.AudioTrack as AndroidAudioTrack

internal class RemoteAudioTrack(
    android: AndroidAudioTrack,
) : MediaStreamTrackImpl(android),
    AudioTrack {
    override fun setVolume(volume: Double) {
        (android as AudioTrack).setVolume(volume)
    }
}
