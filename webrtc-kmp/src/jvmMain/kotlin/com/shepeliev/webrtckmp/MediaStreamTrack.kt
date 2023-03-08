package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.media.MediaStreamTrack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

actual abstract class MediaStreamTrack internal constructor(val native: MediaStreamTrack) {

    actual val id: String
        get() = native.id

    actual val kind: MediaStreamTrackKind
        get() = when (native.kind) {
            MediaStreamTrack.AUDIO_TRACK_KIND -> MediaStreamTrackKind.Audio
            MediaStreamTrack.VIDEO_TRACK_KIND -> MediaStreamTrackKind.Video
            else -> error("Unknown track kind: ${native.kind}")
        }

    actual val label: String
        get() = when (kind) {
            // TODO(shubham): get real capturing device (front/back camera, internal microphone, headset)
            MediaStreamTrackKind.Audio -> "microphone"
            MediaStreamTrackKind.Video -> "camera"
        }

    actual var enabled: Boolean
        get() = native.isEnabled
        set(value) {
            if (value == native.isEnabled) return
            native.isEnabled = value
            onSetEnabled(value)
        }

    private val _state = MutableStateFlow(getInitialState())
    actual val state: StateFlow<MediaStreamTrackState> = _state.asStateFlow()

    actual fun stop() {
        if (_state.value is MediaStreamTrackState.Ended) return
        _state.update { MediaStreamTrackState.Ended(it.muted) }
        onStop()
    }

    protected fun setMuted(muted: Boolean) {
        if (muted) {
            _state.update { it.mute() }
        } else {
            _state.update { it.unmute() }
        }
    }

    protected abstract fun onSetEnabled(enabled: Boolean)

    protected abstract fun onStop()

    private fun getInitialState(): MediaStreamTrackState {
        return when (checkNotNull(native.state)) {
            dev.onvoid.webrtc.media.MediaStreamTrackState.LIVE -> MediaStreamTrackState.Live(muted = false)
            dev.onvoid.webrtc.media.MediaStreamTrackState.ENDED -> MediaStreamTrackState.Ended(muted = false)
        }
    }
}

internal fun MediaStreamTrack.asCommon(): com.shepeliev.webrtckmp.MediaStreamTrack {
    return when (this) {
        is dev.onvoid.webrtc.media.audio.AudioTrack -> AudioStreamTrack(this)
        is dev.onvoid.webrtc.media.video.VideoTrack -> VideoStreamTrack(this)
        else -> error("Unknown native MediaStreamTrack: $this")
    }
}
