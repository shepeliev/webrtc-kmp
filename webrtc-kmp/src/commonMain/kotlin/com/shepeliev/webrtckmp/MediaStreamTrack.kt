package com.shepeliev.webrtckmp

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map

interface MediaStreamTrack {
    val id: String
    val kind: MediaStreamTrackKind
    val label: String
    var enabled: Boolean
    val state: StateFlow<MediaStreamTrackState>
    val constraints: MediaTrackConstraints

    fun stop()
}

sealed interface MediaStreamTrackState {
    val muted: Boolean
    fun mute(): MediaStreamTrackState
    fun unmute(): MediaStreamTrackState

    data class Live(override val muted: Boolean) : MediaStreamTrackState {
        override fun mute(): MediaStreamTrackState = copy(muted = true)
        override fun unmute(): MediaStreamTrackState = copy(muted = false)
    }

    data class Ended(override val muted: Boolean) : MediaStreamTrackState {
        override fun mute(): MediaStreamTrackState = copy(muted = true)
        override fun unmute(): MediaStreamTrackState = copy(muted = false)
    }
}

enum class MediaStreamTrackKind { Audio, Video }

val MediaStreamTrack.muted: Boolean get() = state.value.muted

val MediaStreamTrack.readyState: MediaStreamTrackState get() = state.value

val MediaStreamTrack.onEnded: Flow<Unit>
    get() = state.filter { it is MediaStreamTrackState.Ended }.map { }

val MediaStreamTrack.onMute: Flow<Unit> get() = state.filter { it.muted }.map { }

val MediaStreamTrack.onUnmute: Flow<Unit> get() = state.filterNot { it.muted }.map { }
