@file:OptIn(ExperimentalForeignApi::class)

package com.shepeliev.webrtckmp

import WebRTC.RTCAudioTrack
import kotlinx.cinterop.ExperimentalForeignApi

internal class RemoteAudioStreamTrack(
    ios: RTCAudioTrack
) : MediaStreamTrackImpl(ios), AudioStreamTrack
