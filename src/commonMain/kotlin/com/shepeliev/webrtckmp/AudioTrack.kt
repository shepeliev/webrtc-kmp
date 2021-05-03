package com.shepeliev.webrtckmp

expect class AudioTrack : MediaStreamTrack {
    fun setVolume(volume: Double)
}
