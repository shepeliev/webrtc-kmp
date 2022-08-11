package com.shepeliev.webrtckmp

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.w3c.dom.mediacapture.ENDED
import org.w3c.dom.mediacapture.LIVE
import org.w3c.dom.mediacapture.MediaStreamTrack as JsMediaStreamTrack
import org.w3c.dom.mediacapture.MediaStreamTrackState as JsMediaStreamTrackState

actual abstract class MediaStreamTrack internal constructor(val js: JsMediaStreamTrack) {
    actual val id: String
        get() = js.id

    actual val kind: MediaStreamTrackKind
        get() = js.kind.toMediaStreamTrackKind()

    actual val label: String
        get() = js.label

    actual var enabled: Boolean
        get() = js.enabled
        set(value) {
            js.enabled = value
        }

    private val _state = MutableStateFlow(getInitialState())
    actual val state: StateFlow<MediaStreamTrackState> = _state.asStateFlow()

    init {
        js.onended = { _state.update { MediaStreamTrackState.Ended(js.muted) } }
        js.onmute = { _state.update { it.mute() } }
        js.onunmute = { _state.update { it.unmute() } }
    }

    actual fun stop() {
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

internal fun JsMediaStreamTrack.asCommon(): MediaStreamTrack = when (kind) {
    "audio" -> AudioStreamTrack(this)
    "video" -> VideoStreamTrack(this)
    else -> error("Unknown kind of media stream track: $kind")
}
