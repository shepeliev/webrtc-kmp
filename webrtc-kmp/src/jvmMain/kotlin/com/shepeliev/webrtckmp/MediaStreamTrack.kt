@file:JvmName("AndroidMediaStreamTrack")

package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.media.audio.AudioTrack
import dev.onvoid.webrtc.media.video.VideoTrack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

actual abstract class MediaStreamTrack internal constructor(val jvm: dev.onvoid.webrtc.media.MediaStreamTrack) {

    actual val id: String
        get() = jvm.id

    actual val kind: MediaStreamTrackKind
        get() = when (jvm.kind) {
            dev.onvoid.webrtc.media.MediaStreamTrack.AUDIO_TRACK_KIND -> MediaStreamTrackKind.Audio
            dev.onvoid.webrtc.media.MediaStreamTrack.VIDEO_TRACK_KIND -> MediaStreamTrackKind.Video
            else -> error("Unknown track kind: ${jvm.kind}")
        }

    actual val label: String
        get() = when (kind) {
            // TODO(shepeliev): get real capturing device (front/back camera, internal microphone, headset)
            MediaStreamTrackKind.Audio -> "microphone"
            MediaStreamTrackKind.Video -> "camera"
        }

    actual var enabled: Boolean
        get() = jvm.isEnabled
        set(value) {
            if (value == jvm.isEnabled) return
            jvm.setEnabled(value)
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
        return when (checkNotNull(jvm.state)) {
            dev.onvoid.webrtc.media.MediaStreamTrackState.LIVE -> MediaStreamTrackState.Live(muted = false)
            dev.onvoid.webrtc.media.MediaStreamTrackState.ENDED -> MediaStreamTrackState.Live(muted = false)
        }
    }
}

internal fun MediaStreamTrack.asCommon(): MediaStreamTrack {
    return when (this.jvm) {
        is AudioTrack -> AudioStreamTrack(this.jvm)
        is VideoTrack -> VideoStreamTrack(this.jvm)
        else -> error("Unknown native MediaStreamTrack: $this")
    }
}
