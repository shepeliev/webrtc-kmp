package com.shepeliev.webrtckmp

import WebRTC.RTCMediaStreamTrack
import WebRTC.RTCMediaStreamTrackState
import WebRTC.RTCRtpMediaType
import WebRTC.kRTCMediaStreamTrackKindAudio
import WebRTC.kRTCMediaStreamTrackKindVideo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

abstract class MediaStreamTrackImpl(val native: RTCMediaStreamTrack) : MediaStreamTrack {
    override val id: String = native.trackId
    override val kind: MediaStreamTrackKind = native.kind().toMediaStreamTrackKind()

    override val label: String
        get() = when (kind) {
            // TODO(shepeliev): get real capturing device (front/back camera, internal microphone, headset)
            MediaStreamTrackKind.Audio -> "microphone"
            MediaStreamTrackKind.Video -> "camera"
            MediaStreamTrackKind.Data -> "data"
        }

    override var enabled: Boolean
        get() = native.isEnabled
        set(value) {
            if (native.isEnabled == value) return
            native.isEnabled = value
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
        return when (native.readyState) {
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

internal fun MediaStreamTrackKind.asNative(): RTCRtpMediaType = when (this) {
    MediaStreamTrackKind.Audio -> RTCRtpMediaType.RTCRtpMediaTypeAudio
    MediaStreamTrackKind.Video -> RTCRtpMediaType.RTCRtpMediaTypeVideo
    MediaStreamTrackKind.Data -> RTCRtpMediaType.RTCRtpMediaTypeData
}

internal fun String.toMediaStreamTrackKind(): MediaStreamTrackKind = when (this) {
    kRTCMediaStreamTrackKindAudio -> MediaStreamTrackKind.Audio
    kRTCMediaStreamTrackKindVideo -> MediaStreamTrackKind.Video
    else -> error("Unknown track kind: $this")
}
