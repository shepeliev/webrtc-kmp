package com.shepeliev.webrtckmp

import kotlinx.coroutines.flow.Flow

expect open class MediaStreamTrack {
    val id: String
    val kind: MediaStreamTrackKind
    val label: String
    val muted: Boolean
    val readOnly: Boolean
    val readyState: MediaStreamTrackState
    val remote: Boolean

    var enabled: Boolean

    val onEnded: Flow<Unit>
    val onMute: Flow<Unit>
    val onUnmute: Flow<Unit>

    fun stop()
}

enum class MediaStreamTrackState { Live, Ended }

enum class MediaStreamTrackKind { Audio, Video }

