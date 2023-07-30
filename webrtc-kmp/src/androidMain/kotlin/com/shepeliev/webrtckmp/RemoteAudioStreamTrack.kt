package com.shepeliev.webrtckmp

import org.webrtc.AudioTrack

internal class RemoteAudioStreamTrack(
    android: AudioTrack
) : MediaStreamTrackImpl(android), AudioStreamTrack
