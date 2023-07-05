package com.shepeliev.webrtckmp

import WebRTC.RTCAudioTrack

internal class LocalAudioStreamTrack(
    ios: RTCAudioTrack,
    override val constraints: MediaTrackConstraints,
) : MediaStreamTrackImpl(ios), AudioStreamTrack
