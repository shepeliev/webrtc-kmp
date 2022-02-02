package com.shepeliev.webrtckmp

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.webrtc.MediaSource
import org.webrtc.AudioTrack as AndroidAudioTrack
import org.webrtc.MediaStreamTrack as AndroidMediaStreamTrack
import org.webrtc.VideoTrack as AndroidVideoTrack

actual open class MediaStreamTrack internal constructor(
    val android: AndroidMediaStreamTrack,
    private val mediaSource: MediaSource?,
) {

    actual val id: String
        get() = android.id()

    actual val kind: MediaStreamTrackKind
        get() = when (android.kind()) {
            AndroidMediaStreamTrack.AUDIO_TRACK_KIND -> MediaStreamTrackKind.Audio
            AndroidMediaStreamTrack.VIDEO_TRACK_KIND -> MediaStreamTrackKind.Video
            else -> throw IllegalStateException("Unknown track kind: ${android.kind()}")
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
        get() = android.enabled()
        set(value) {
            android.setEnabled(value)
            if (value) {
                _onUnmute.tryEmit(Unit)
            } else {
                _onMute.tryEmit(Unit)
            }
        }

    actual val readyState: MediaStreamTrackState
        get() = android.state().asCommon()

    private val _onEnded = MutableSharedFlow<Unit>(extraBufferCapacity = FLOW_BUFFER_CAPACITY)
    actual val onEnded: Flow<Unit> = _onEnded.asSharedFlow()

    private val _onMute = MutableSharedFlow<Unit>(extraBufferCapacity = FLOW_BUFFER_CAPACITY)
    actual val onMute: Flow<Unit> = _onMute.asSharedFlow()

    private val _onUnmute = MutableSharedFlow<Unit>(extraBufferCapacity = FLOW_BUFFER_CAPACITY)
    actual val onUnmute: Flow<Unit> = _onUnmute.asSharedFlow()

    actual open fun stop() {
        if (readyState == MediaStreamTrackState.Ended) return
        _onEnded.tryEmit(Unit)
        mediaSource?.dispose()
    }
}

internal fun AndroidMediaStreamTrack.asCommon(): MediaStreamTrack {
    return when (this) {
        is AndroidAudioTrack -> AudioStreamTrack(this)
        is AndroidVideoTrack -> VideoStreamTrack(this)
        else -> error("Unknown native MediaStreamTrack: $this")
    }
}

private fun AndroidMediaStreamTrack.State.asCommon(): MediaStreamTrackState {
    return when (this) {
        AndroidMediaStreamTrack.State.LIVE -> MediaStreamTrackState.Live
        AndroidMediaStreamTrack.State.ENDED -> MediaStreamTrackState.Ended
    }
}
