package com.shepeliev.webrtckmp

import org.webrtc.AudioSource as NativeAudioSource

actual class AudioSource internal constructor(override val native: NativeAudioSource) :
    BaseMediaSource(), MediaSource {

    actual override val state: MediaSource.State
        get() = super.state
}
