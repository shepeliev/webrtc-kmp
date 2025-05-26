package com.shepeliev.webrtckmp.internal

import com.shepeliev.webrtckmp.AudioTrack
import com.shepeliev.webrtckmp.MediaStreamTrackImpl
import com.shepeliev.webrtckmp.externals.PlatformMediaStreamTrack

internal class AudioTrackImpl(
    platform: PlatformMediaStreamTrack,
) : MediaStreamTrackImpl(platform),
    AudioTrack {
    override fun setVolume(volume: Double) {
        // not available in JS
    }
}
