@file:JvmName("AndroidMediaStreamTrack")

package com.shepeliev.webrtckmp

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.webrtc.MediaStreamTrack as AndroidMediaStreamTrack

abstract class MediaStreamTrackImpl(
    val native: AndroidMediaStreamTrack
) : MediaStreamTrack {

    override val id: String
        get() = native.id()

    override val kind: MediaStreamTrackKind
        get() = when (native.kind()) {
            AndroidMediaStreamTrack.AUDIO_TRACK_KIND -> MediaStreamTrackKind.Audio
            AndroidMediaStreamTrack.VIDEO_TRACK_KIND -> MediaStreamTrackKind.Video
            else -> error("Unknown track kind: ${native.kind()}")
        }

    override val label: String
        get() = when (kind) {
            // TODO(shepeliev): get real capturing device (front/back camera, internal microphone, headset)
            MediaStreamTrackKind.Audio -> "microphone"
            MediaStreamTrackKind.Video -> "camera"
        }

    override var enabled: Boolean
        get() = native.enabled()
        set(value) {
            if (value == native.enabled()) return
            native.setEnabled(value)
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

    protected open fun setMuted(muted: Boolean) {
        if (muted) {
            _state.update { it.mute() }
        } else {
            _state.update { it.unmute() }
        }
    }

    protected open fun onSetEnabled(enabled: Boolean) {}

    protected open fun onStop() {}

    private fun getInitialState(): MediaStreamTrackState {
        return when (checkNotNull(native.state())) {
            AndroidMediaStreamTrack.State.LIVE -> MediaStreamTrackState.Live(muted = false)
            AndroidMediaStreamTrack.State.ENDED -> MediaStreamTrackState.Live(muted = false)
        }
    }
}
