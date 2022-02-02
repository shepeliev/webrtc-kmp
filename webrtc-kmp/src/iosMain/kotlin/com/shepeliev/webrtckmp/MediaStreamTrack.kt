package com.shepeliev.webrtckmp

import WebRTC.RTCAudioTrack
import WebRTC.RTCMediaStreamTrack
import WebRTC.RTCMediaStreamTrackState
import WebRTC.RTCVideoTrack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.native.concurrent.AtomicInt

actual open class MediaStreamTrack internal constructor(val ios: RTCMediaStreamTrack) {

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
                _onUnmute.tryEmit(Unit)
            } else {
                _onMute.tryEmit(Unit)
            }
        }

    actual val readyState: MediaStreamTrackState
        get() {
            if (endedFlag.value == 1) {
                return MediaStreamTrackState.Ended
            }
            return rtcMediaStreamTrackStateAsCommon(ios.readyState)
        }

    private val _onEnded = MutableSharedFlow<Unit>(extraBufferCapacity = FLOW_BUFFER_CAPACITY)
    actual val onEnded: Flow<Unit> = _onEnded.asSharedFlow()

    private val _onMute = MutableSharedFlow<Unit>(extraBufferCapacity = FLOW_BUFFER_CAPACITY)
    actual val onMute: Flow<Unit> = _onMute.asSharedFlow()

    private val _onUnmute = MutableSharedFlow<Unit>(extraBufferCapacity = FLOW_BUFFER_CAPACITY)
    actual val onUnmute: Flow<Unit> = _onUnmute.asSharedFlow()

    private val endedFlag = AtomicInt(0)

    actual open fun stop() {
        if (!endedFlag.compareAndSet(0, 1)) return
        enabled = false
        _onEnded.tryEmit(Unit)
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
        else -> error("Unknown RTCMediaStreamTrackState: $state")
    }
}
