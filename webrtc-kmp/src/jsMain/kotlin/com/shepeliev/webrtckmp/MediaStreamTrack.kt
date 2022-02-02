package com.shepeliev.webrtckmp

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.w3c.dom.mediacapture.ENDED
import org.w3c.dom.mediacapture.LIVE
import org.w3c.dom.mediacapture.MediaStreamTrack as JsMediaStreamTrack
import org.w3c.dom.mediacapture.MediaStreamTrackState as JsMediaStreamTrackState

actual open class MediaStreamTrack internal constructor(val js: JsMediaStreamTrack) {
    private val scope = MainScope()

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

    private val onEndedInternal = MutableSharedFlow<Unit>()
    actual val onEnded: Flow<Unit> = onEndedInternal.asSharedFlow()

    private val onMuteInternal = MutableSharedFlow<Unit>()
    actual val onMute: Flow<Unit> = onEndedInternal.asSharedFlow()

    private val onUnmuteInternal = MutableSharedFlow<Unit>()
    actual val onUnmute: Flow<Unit> = onEndedInternal.asSharedFlow()

    init {
        js.onended = {
            scope.launch {
                onEndedInternal.emit(Unit)
                cancel()
            }
        }

        js.onmute = {
            scope.launch { onMuteInternal.emit(Unit) }
        }

        js.onunmute = {
            scope.launch { onUnmuteInternal.emit(Unit) }
        }
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
