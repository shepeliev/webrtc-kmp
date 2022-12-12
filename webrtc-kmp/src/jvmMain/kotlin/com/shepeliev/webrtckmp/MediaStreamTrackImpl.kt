@file:JvmName("JVMMediaStreamTrack")

package com.shepeliev.webrtckmp

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import dev.onvoid.webrtc.media.MediaStreamTrack as NativeMediaStreamTrack

internal abstract class MediaStreamTrackImpl(
    val native: NativeMediaStreamTrack
) : MediaStreamTrack {

    override val id: String
        get() = native.id

    override val kind: MediaStreamTrackKind
        get() = when (native.kind) {
            NativeMediaStreamTrack.AUDIO_TRACK_KIND -> MediaStreamTrackKind.Audio
            NativeMediaStreamTrack.VIDEO_TRACK_KIND -> MediaStreamTrackKind.Video
            else -> error("Unknown track kind: ${native.kind}")
        }

    override val label: String
        get() = when (kind) {
            MediaStreamTrackKind.Audio -> "microphone"
            MediaStreamTrackKind.Video -> "camera"
        }

    override var enabled: Boolean
        get() = native.isEnabled
        set(value) {
            if (value == native.isEnabled) return
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
        return when (checkNotNull(native.state)) {
            dev.onvoid.webrtc.media.MediaStreamTrackState.LIVE -> MediaStreamTrackState.Live(muted = false)
            dev.onvoid.webrtc.media.MediaStreamTrackState.ENDED -> MediaStreamTrackState.Ended(muted = false)
        }
    }
}
