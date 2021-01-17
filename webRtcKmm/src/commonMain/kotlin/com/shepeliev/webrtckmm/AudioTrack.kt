package com.shepeliev.webrtckmm

expect class AudioTrack : MediaStreamTrack {
    fun setVolume(volume: Double)
}
