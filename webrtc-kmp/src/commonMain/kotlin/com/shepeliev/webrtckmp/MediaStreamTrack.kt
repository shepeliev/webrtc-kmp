package com.shepeliev.webrtckmp

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map

public interface MediaStreamTrack {
    public val id: String
    public val kind: MediaStreamTrackKind
    public val label: String
    public var enabled: Boolean
    public val state: StateFlow<MediaStreamTrackState>
    public val constraints: MediaTrackConstraints
    public val settings: MediaTrackSettings

    public fun stop()
}

public sealed interface MediaStreamTrackState {
    public val muted: Boolean

    public fun mute(): MediaStreamTrackState

    public fun unmute(): MediaStreamTrackState

    public data class Live(
        override val muted: Boolean,
    ) : MediaStreamTrackState {
        override fun mute(): MediaStreamTrackState = copy(muted = true)

        override fun unmute(): MediaStreamTrackState = copy(muted = false)
    }

    public data class Ended(
        override val muted: Boolean,
    ) : MediaStreamTrackState {
        override fun mute(): MediaStreamTrackState = copy(muted = true)

        override fun unmute(): MediaStreamTrackState = copy(muted = false)
    }
}

public enum class MediaStreamTrackKind { Audio, Video }

public val MediaStreamTrack.muted: Boolean get() = state.value.muted

public val MediaStreamTrack.readyState: MediaStreamTrackState get() = state.value

public val MediaStreamTrack.onEnded: Flow<Unit>
    get() = state.filter { it is MediaStreamTrackState.Ended }.map { }

public val MediaStreamTrack.onMute: Flow<Unit> get() = state.filter { it.muted }.map { }

public val MediaStreamTrack.onUnmute: Flow<Unit> get() = state.filterNot { it.muted }.map { }
