package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.media.audio.AudioTrack

internal class RemoteAudioStreamTrack(
    native: AudioTrack
) : MediaStreamTrackImpl(native), AudioStreamTrack
