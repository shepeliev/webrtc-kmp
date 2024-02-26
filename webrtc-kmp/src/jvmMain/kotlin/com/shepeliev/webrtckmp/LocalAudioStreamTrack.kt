package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.media.audio.AudioTrack
import dev.onvoid.webrtc.media.audio.AudioTrackSource

internal class LocalAudioStreamTrack(
    native: AudioTrack,
    private val audioSource: AudioTrackSource,
    override val constraints: MediaTrackConstraints,
) : MediaStreamTrackImpl(native), AudioStreamTrack
