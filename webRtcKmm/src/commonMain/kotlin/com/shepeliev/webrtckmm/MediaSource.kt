package com.shepeliev.webrtckmm

interface MediaSource {
    val state: State

    enum class State { Initializing, Live, Ended, Muted }
}
