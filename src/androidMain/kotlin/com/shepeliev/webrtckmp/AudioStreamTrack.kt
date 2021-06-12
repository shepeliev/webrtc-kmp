package com.shepeliev.webrtckmp

import org.webrtc.AudioTrack
import org.webrtc.MediaSource

actual class AudioStreamTrack internal constructor(
    android: AudioTrack,
    mediaSource: MediaSource? = null,
) : MediaStreamTrack(android, mediaSource)
