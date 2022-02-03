package com.shepeliev.webrtckmp

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    actual val muted: Boolean
        get() = js.muted

    actual val readyState: MediaStreamTrackState
        get() = js.readyState.toMediaStreamTrackState()

    actual var enabled: Boolean
        get() = js.enabled
        set(value) {
            js.enabled = value
        }

    private val _onEnded = MutableSharedFlow<Unit>(extraBufferCapacity = FLOW_BUFFER_CAPACITY)
    actual val onEnded: Flow<Unit> = _onEnded.asSharedFlow()

    private val _onMute = MutableSharedFlow<Unit>(extraBufferCapacity = FLOW_BUFFER_CAPACITY)
    actual val onMute: Flow<Unit> = _onEnded.asSharedFlow()

    private val _onUnmute = MutableSharedFlow<Unit>(extraBufferCapacity = FLOW_BUFFER_CAPACITY)
    actual val onUnmute: Flow<Unit> = _onEnded.asSharedFlow()

    init {
        js.onended = { _onEnded.tryEmit(Unit) }
        js.onmute = { _onMute.tryEmit(Unit) }
        js.onunmute = { _onUnmute.tryEmit(Unit) }
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

    private fun JsMediaStreamTrackState.toMediaStreamTrackState(): MediaStreamTrackState {
        return when (this) {
            JsMediaStreamTrackState.LIVE -> MediaStreamTrackState.Live
            JsMediaStreamTrackState.ENDED -> MediaStreamTrackState.Ended
            else -> error("Unknown media stream track state: $this")
        }
    }
}

internal fun JsMediaStreamTrack.asCommon(): MediaStreamTrack = when (kind) {
    "audio" -> AudioStreamTrack(this)
    "video" -> VideoStreamTrack(this)
    else -> error("Unknown kind of media stream track: $kind")
}
