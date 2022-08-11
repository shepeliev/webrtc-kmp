@file:JvmName("AndroidMediaStreamTrack")

package com.shepeliev.webrtckmp

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.webrtc.AudioTrack as AndroidAudioTrack
import org.webrtc.MediaStreamTrack as AndroidMediaStreamTrack
import org.webrtc.VideoTrack as AndroidVideoTrack

actual abstract class MediaStreamTrack internal constructor(val android: AndroidMediaStreamTrack) {

    actual val id: String
        get() = android.id()

    actual val kind: MediaStreamTrackKind
        get() = when (android.kind()) {
            AndroidMediaStreamTrack.AUDIO_TRACK_KIND -> MediaStreamTrackKind.Audio
            AndroidMediaStreamTrack.VIDEO_TRACK_KIND -> MediaStreamTrackKind.Video
            else -> error("Unknown track kind: ${android.kind()}")
        }

    actual val label: String
        get() = when (kind) {
            // TODO(shepeliev): get real capturing device (front/back camera, internal microphone, headset)
            MediaStreamTrackKind.Audio -> "microphone"
            MediaStreamTrackKind.Video -> "camera"
        }

    actual var enabled: Boolean
        get() = android.enabled()
        set(value) {
            android.setEnabled(value)
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
        return when (checkNotNull(android.state())) {
            AndroidMediaStreamTrack.State.LIVE -> MediaStreamTrackState.Live(muted = false)
            AndroidMediaStreamTrack.State.ENDED -> MediaStreamTrackState.Live(muted = false)
        }
    }
}

internal fun AndroidMediaStreamTrack.asCommon(): MediaStreamTrack {
    return when (this) {
        is AndroidAudioTrack -> AudioStreamTrack(this)
        is AndroidVideoTrack -> VideoStreamTrack(this)
        else -> error("Unknown native MediaStreamTrack: $this")
    }
}
