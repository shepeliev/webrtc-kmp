package com.shepeliev.webrtckmp.internal

import com.shepeliev.webrtckmp.AudioStreamTrack
import com.shepeliev.webrtckmp.MediaStreamTrackImpl
import com.shepeliev.webrtckmp.externals.PlatformMediaStreamTrack

internal class AudioTrackImpl(platform: PlatformMediaStreamTrack) : MediaStreamTrackImpl(platform),
    AudioStreamTrack {

    override fun setVolume(volume: Double) {
        // not available in JS
    }
}
