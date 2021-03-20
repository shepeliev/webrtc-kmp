package com.shepeliev.webrtckmm

expect class AudioSource : MediaSource {
    override val state: MediaSource.State
}
