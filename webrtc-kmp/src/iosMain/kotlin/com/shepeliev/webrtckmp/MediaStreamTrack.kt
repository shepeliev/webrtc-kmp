package com.shepeliev.webrtckmp

import WebRTC.RTCAudioTrack
import WebRTC.RTCMediaStreamTrack
import WebRTC.RTCMediaStreamTrackState
import WebRTC.RTCVideoTrack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlin.native.concurrent.AtomicInt

actual abstract class MediaStreamTrack internal constructor(val ios: RTCMediaStreamTrack) {

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

    actual val readyState: MediaStreamTrackState
        get() {
            if (endedFlag.value == 1) {
                return MediaStreamTrackState.Ended
            }
            return rtcMediaStreamTrackStateAsCommon(ios.readyState)
        }

    // not implemented for iOS
    actual val muted: Boolean = false

    actual var enabled: Boolean
        get() = ios.isEnabled
        set(value) {
            ios.isEnabled = value
            onSetEnabled(value)
        }

    private val _onEnded = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    actual val onEnded: Flow<Unit> = _onEnded.asSharedFlow()

    actual val onMute: Flow<Unit> = emptyFlow()

    actual val onUnmute: Flow<Unit> = emptyFlow()

    private val endedFlag = AtomicInt(0)

    actual fun stop() {
        if (!endedFlag.compareAndSet(0, 1)) return
        enabled = false
        _onEnded.tryEmit(Unit)
        onStop()
    }

    protected abstract fun onSetEnabled(enabled: Boolean)

    protected abstract fun onStop()

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
        else -> error("Unknown RTCMediaStreamTrackState: $state")
    }
}
