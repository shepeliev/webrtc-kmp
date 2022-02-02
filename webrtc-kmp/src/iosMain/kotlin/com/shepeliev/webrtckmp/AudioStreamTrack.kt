package com.shepeliev.webrtckmp

import WebRTC.RTCAudioTrack

actual class AudioStreamTrack internal constructor(ios: RTCAudioTrack) : MediaStreamTrack(ios)
