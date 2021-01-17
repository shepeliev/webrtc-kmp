package com.shepeliev.webrtckmm

interface MediaSource {
    val state: State

    fun dispose()

    enum class State { Initializing, Live, Ended, Muted }
}
