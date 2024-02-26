@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.shepeliev.webrtckmp

import WebRTC.RTCAudioTrack

internal class RemoteAudioStreamTrack(
    ios: RTCAudioTrack
) : MediaStreamTrackImpl(ios), AudioStreamTrack
