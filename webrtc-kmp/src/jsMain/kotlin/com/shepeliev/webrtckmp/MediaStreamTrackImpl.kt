package com.shepeliev.webrtckmp

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.w3c.dom.mediacapture.ENDED
import org.w3c.dom.mediacapture.LIVE
import org.w3c.dom.mediacapture.MediaStreamTrack as JsMediaStreamTrack
import org.w3c.dom.mediacapture.MediaStreamTrackState as JsMediaStreamTrackState

abstract class MediaStreamTrackImpl(val native: JsMediaStreamTrack) : MediaStreamTrack {
    override val id: String
        get() = native.id

    override val kind: MediaStreamTrackKind
        get() = native.kind.toMediaStreamTrackKind()

    override val label: String
        get() = native.label

    override var enabled: Boolean
        get() = native.enabled
        set(value) {
            native.enabled = value
        }

    private val _state = MutableStateFlow(getInitialState())
    override val state: StateFlow<MediaStreamTrackState> = _state.asStateFlow()

    override val constraints: MediaTrackConstraints
        get() = native.getConstraints().asCommon()

    override val settings: MediaTrackSettings
        get() = native.getSettings().asCommon()

    init {
        native.onended = { _state.update { MediaStreamTrackState.Ended(native.muted) } }
        native.onmute = { _state.update { it.mute() } }
        native.onunmute = { _state.update { it.unmute() } }
    }

    override fun stop() {
        native.stop()
    }

    private fun String.toMediaStreamTrackKind(): MediaStreamTrackKind {
        return when (this) {
            "audio" -> MediaStreamTrackKind.Audio
            "video" -> MediaStreamTrackKind.Video
            else -> error("Unknown media stream track kind: $this")
        }
    }

    private fun getInitialState(): MediaStreamTrackState {
        return when (native.readyState) {
            JsMediaStreamTrackState.LIVE -> MediaStreamTrackState.Live(native.muted)
            JsMediaStreamTrackState.ENDED -> MediaStreamTrackState.Ended(native.muted)
            else -> error("Unknown media stream track state: ${native.readyState}")
        }
    }
}

internal fun JsMediaStreamTrack.asCommon(): MediaStreamTrackImpl = when (kind) {
    "audio" -> AudioStreamTrackImpl(this)
    "video" -> VideoStreamTrackImpl(this)
    else -> error("Unknown kind of media stream track: $kind")
}
