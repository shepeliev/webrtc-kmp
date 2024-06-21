package com.shepeliev.webrtckmp

import com.shepeliev.webrtckmp.externals.PlatformMediaStreamTrack
import com.shepeliev.webrtckmp.externals.asCommon
import com.shepeliev.webrtckmp.externals.getConstraints
import com.shepeliev.webrtckmp.externals.toMediaStreamTrackState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal open class MediaStreamTrackImpl(val platform: PlatformMediaStreamTrack) : MediaStreamTrack {
    override val id: String get() = platform.id
    override val kind: MediaStreamTrackKind get() = platform.kind.toMediaStreamTrackKind()
    override val label: String get() = platform.label

    override var enabled: Boolean
        get() = platform.enabled
        set(value) {
            platform.enabled = value
        }

    private val _state = MutableStateFlow(platform.readyState.toMediaStreamTrackState(platform.muted))
    override val state: StateFlow<MediaStreamTrackState> = _state.asStateFlow()

    override val constraints: MediaTrackConstraints get() = platform.getConstraints()

    override val settings: MediaTrackSettings
        get() = platform.getSettings().asCommon()

    init {
        platform.onended = { _state.update { MediaStreamTrackState.Ended(platform.muted) } }
        platform.onmute = { _state.update { it.mute() } }
        platform.onunmute = { _state.update { it.unmute() } }
    }

    override fun stop() {
        platform.stop()
    }

    private fun String.toMediaStreamTrackKind(): MediaStreamTrackKind {
        return when (this) {
            "audio" -> MediaStreamTrackKind.Audio
            "video" -> MediaStreamTrackKind.Video
            else -> error("Unknown media stream track kind: $this")
        }
    }
}
