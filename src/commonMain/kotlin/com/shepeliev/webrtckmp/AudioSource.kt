package com.shepeliev.webrtckmp

expect class AudioSource : MediaSource {
    override val state: MediaSource.State
    fun dispose()
}
