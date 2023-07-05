package com.shepeliev.webrtckmp

import WebRTC.RTCMediaStreamTrack
import WebRTC.RTCMediaStreamTrackState
import WebRTC.kRTCMediaStreamTrackKindAudio
import WebRTC.kRTCMediaStreamTrackKindVideo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal abstract class MediaStreamTrackImpl(val ios: RTCMediaStreamTrack) : MediaStreamTrack {

    override val id: String
        get() = ios.trackId

    override val kind: MediaStreamTrackKind
        get() = when (ios.kind()) {
            kRTCMediaStreamTrackKindAudio -> MediaStreamTrackKind.Audio
            kRTCMediaStreamTrackKindVideo -> MediaStreamTrackKind.Video
            else -> error("Unknown track kind: ${ios.kind()}")
        }

    override val label: String
        get() = when (kind) {
            // TODO(shepeliev): get real capturing device (front/back camera, internal microphone, headset)
            MediaStreamTrackKind.Audio -> "microphone"
            MediaStreamTrackKind.Video -> "camera"
        }

    override var enabled: Boolean
        get() = ios.isEnabled
        set(value) {
            if (ios.isEnabled == value) return
            ios.isEnabled = value
            onSetEnabled(value)
        }

    private val _state = MutableStateFlow(getInitialState())
    override val state: StateFlow<MediaStreamTrackState> = _state.asStateFlow()

    override val constraints: MediaTrackConstraints = MediaTrackConstraints()
    override val settings: MediaTrackSettings = MediaTrackSettings()

    override fun stop() {
        if (_state.value is MediaStreamTrackState.Ended) return
        _state.update { MediaStreamTrackState.Ended(it.muted) }
        onStop()
    }

    protected fun setMute(muted: Boolean) {
        if (muted) {
            _state.update { it.mute() }
        } else {
            _state.update { it.unmute() }
        }
    }

    protected open fun onSetEnabled(enabled: Boolean) {}

    protected open fun onStop() {}

    private fun getInitialState(): MediaStreamTrackState {
        return when (ios.readyState) {
            RTCMediaStreamTrackState.RTCMediaStreamTrackStateLive -> MediaStreamTrackState.Live(
                muted = false
            )

            RTCMediaStreamTrackState.RTCMediaStreamTrackStateEnded -> MediaStreamTrackState.Live(
                muted = false
            )

            else -> error("Unknown RTCMediaStreamTrackState: $state")
        }
    }
}
