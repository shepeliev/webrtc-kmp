package com.shepeliev.webrtckmp

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.w3c.dom.mediacapture.ENDED
import org.w3c.dom.mediacapture.LIVE
import org.w3c.dom.mediacapture.MediaStreamTrack as JsMediaStreamTrack
import org.w3c.dom.mediacapture.MediaStreamTrackState as JsMediaStreamTrackState

internal abstract class MediaStreamTrackImpl(val js: JsMediaStreamTrack) : MediaStreamTrack {
    override val id: String
        get() = js.id

    override val kind: MediaStreamTrackKind
        get() = js.kind.toMediaStreamTrackKind()

    override val label: String
        get() = js.label

    override var enabled: Boolean
        get() = js.enabled
        set(value) {
            js.enabled = value
        }

    private val _state = MutableStateFlow(getInitialState())
    override val state: StateFlow<MediaStreamTrackState> = _state.asStateFlow()

    override val constraints: MediaTrackConstraints
        get() = js.getConstraints().asCommon()

    init {
        js.onended = { _state.update { MediaStreamTrackState.Ended(js.muted) } }
        js.onmute = { _state.update { it.mute() } }
        js.onunmute = { _state.update { it.unmute() } }
    }

    override fun stop() {
        js.stop()
    }

    private fun String.toMediaStreamTrackKind(): MediaStreamTrackKind {
        return when (this) {
            "audio" -> MediaStreamTrackKind.Audio
            "video" -> MediaStreamTrackKind.Video
            else -> error("Unknown media stream track kind: $this")
        }
    }

    private fun getInitialState(): MediaStreamTrackState {
        return when (js.readyState) {
            JsMediaStreamTrackState.LIVE -> MediaStreamTrackState.Live(js.muted)
            JsMediaStreamTrackState.ENDED -> MediaStreamTrackState.Ended(js.muted)
            else -> error("Unknown media stream track state: ${js.readyState}")
        }
    }
}

internal fun JsMediaStreamTrack.asCommon(): MediaStreamTrackImpl = when (kind) {
    "audio" -> AudioStreamTrackImpl(this)
    "video" -> VideoStreamTrackImpl(this)
    else -> error("Unknown kind of media stream track: $kind")
}
