package com.shepeliev.webrtckmp

import WebRTC.RTCAudioTrack

internal class AudioStreamTrackImpl(
    ios: RTCAudioTrack
) : MediaStreamTrackImpl(ios), AudioStreamTrack
