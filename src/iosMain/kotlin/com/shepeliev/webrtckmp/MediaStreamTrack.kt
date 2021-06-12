package com.shepeliev.webrtckmp

import WebRTC.RTCAudioTrack
import WebRTC.RTCMediaStreamTrack
import WebRTC.RTCMediaStreamTrackState
import WebRTC.RTCVideoTrack
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlin.native.concurrent.AtomicInt

actual open class MediaStreamTrack internal constructor(val ios: RTCMediaStreamTrack) {

    protected val scope = MainScope()

    actual val id: String
        get() = ios.trackId

    actual val kind: MediaStreamTrackKind
        get() = when (ios.kind()) {
            "audio" -> MediaStreamTrackKind.Audio
            "video" -> MediaStreamTrackKind.Video
            else -> throw IllegalStateException("Unknown track kind: ${ios.kind()}")
        }

    actual val label: String
        get() = when (kind) {
            // TODO(shepeliev): get real capturing device (front/back camera, internal microphone, headset)
            MediaStreamTrackKind.Audio -> "microphone"
            MediaStreamTrackKind.Video -> "camera"
        }

    actual val muted: Boolean
        get() = !enabled

    actual var enabled: Boolean
        get() = ios.isEnabled
        set(value) {
            ios.isEnabled = value
            if (value) {
                scope.launch { onUnmuteInternal.emit(Unit) }
            } else {
                scope.launch { onMuteInternal.emit(Unit) }
            }
        }

    actual val readyState: MediaStreamTrackState
        get() {
            if (endedFlag.value == 1) {
                return MediaStreamTrackState.Ended
            }
            return rtcMediaStreamTrackStateAsCommon(ios.readyState)
        }

    private val onEndedInternal = MutableSharedFlow<Unit>()
    actual val onEnded: Flow<Unit> = onEndedInternal.asSharedFlow()

    private val onMuteInternal = MutableSharedFlow<Unit>()
    actual val onMute: Flow<Unit> = onMuteInternal.asSharedFlow()

    private val onUnmuteInternal = MutableSharedFlow<Unit>()
    actual val onUnmute: Flow<Unit> = onUnmuteInternal.asSharedFlow()

    private val endedFlag = AtomicInt(0)

    actual open fun stop() {
        if (!endedFlag.compareAndSet(0, 1)) return
        enabled = false
        scope.launch {
            onEndedInternal.emit(Unit)
            scope.cancel()
        }
    }

    companion object {
        fun createCommon(ios: RTCMediaStreamTrack): MediaStreamTrack {
            return when (ios) {
                is RTCAudioTrack -> AudioStreamTrack(ios)
                is RTCVideoTrack -> VideoStreamTrack(ios)
                else -> error("Unknown native MediaStreamTrack: $this")
            }
        }
    }
}

private fun rtcMediaStreamTrackStateAsCommon(state: RTCMediaStreamTrackState): MediaStreamTrackState {
    return when (state) {
        RTCMediaStreamTrackState.RTCMediaStreamTrackStateLive -> MediaStreamTrackState.Live
        RTCMediaStreamTrackState.RTCMediaStreamTrackStateEnded -> MediaStreamTrackState.Ended
    }
}
