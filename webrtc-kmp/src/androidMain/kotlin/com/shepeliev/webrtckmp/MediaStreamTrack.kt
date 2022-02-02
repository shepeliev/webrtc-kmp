package com.shepeliev.webrtckmp

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.emptyFlow
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

    // not implemented for Android
    actual val muted: Boolean = false

    actual var enabled: Boolean
        get() = android.enabled()
        set(value) {
            android.setEnabled(value)
        }

    actual val readyState: MediaStreamTrackState
        get() = android.state().asCommon()

    private val _onEnded = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    actual val onEnded: Flow<Unit> = _onEnded.asSharedFlow()

    // not implemented for Android
    actual val onMute: Flow<Unit> = emptyFlow()

    // not implemented for Android
    actual val onUnmute: Flow<Unit> = emptyFlow()

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
