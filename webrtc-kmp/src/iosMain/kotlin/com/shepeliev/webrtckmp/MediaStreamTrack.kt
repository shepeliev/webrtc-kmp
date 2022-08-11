package com.shepeliev.webrtckmp

import WebRTC.RTCAudioTrack
import WebRTC.RTCMediaStreamTrack
import WebRTC.RTCMediaStreamTrackState
import WebRTC.RTCVideoTrack
import WebRTC.kRTCMediaStreamTrackKindAudio
import WebRTC.kRTCMediaStreamTrackKindVideo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

actual abstract class MediaStreamTrack internal constructor(val ios: RTCMediaStreamTrack) {

    actual val id: String
        get() = ios.trackId

    actual val kind: MediaStreamTrackKind
        get() = when (ios.kind()) {
            kRTCMediaStreamTrackKindAudio -> MediaStreamTrackKind.Audio
            kRTCMediaStreamTrackKindVideo -> MediaStreamTrackKind.Video
            else -> error("Unknown track kind: ${ios.kind()}")
        }

    actual val label: String
        get() = when (kind) {
            // TODO(shepeliev): get real capturing device (front/back camera, internal microphone, headset)
            MediaStreamTrackKind.Audio -> "microphone"
            MediaStreamTrackKind.Video -> "camera"
        }

    actual var enabled: Boolean
        get() = ios.isEnabled
        set(value) {
            ios.isEnabled = value
            onSetEnabled(value)
        }

    private val _state = MutableStateFlow(getInitialState())
    actual val state: StateFlow<MediaStreamTrackState> = _state.asStateFlow()

    actual fun stop() {
        if (_state.value is MediaStreamTrackState.Ended) return
        _state.update { MediaStreamTrackState.Ended(it.muted) }
        onStop()
    }

    protected abstract fun onSetEnabled(enabled: Boolean)

    protected abstract fun onStop()

    private fun getInitialState(): MediaStreamTrackState {
        return when (ios.readyState) {
            RTCMediaStreamTrackState.RTCMediaStreamTrackStateLive -> MediaStreamTrackState.Live(muted = false)
            RTCMediaStreamTrackState.RTCMediaStreamTrackStateEnded -> MediaStreamTrackState.Live(muted = false)
            else -> error("Unknown RTCMediaStreamTrackState: $state")
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
