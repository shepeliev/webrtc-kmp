package com.shepeliev.webrtckmm

interface MediaSource {
    val state: State

    // TODO remove dispose from common code
    fun dispose()

    enum class State { Initializing, Live, Ended, Muted }
}
