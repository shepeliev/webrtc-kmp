package com.shepeliev.webrtckmp

interface MediaSource {
    val state: State

    enum class State { Initializing, Live, Ended, Muted }
}
