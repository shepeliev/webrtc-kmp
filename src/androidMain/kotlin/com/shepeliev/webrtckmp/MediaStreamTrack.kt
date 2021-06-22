package com.shepeliev.webrtckmp

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.webrtc.MediaSource
import org.webrtc.AudioTrack as AndroidAudioTrack
import org.webrtc.MediaStreamTrack as AndroidMediaStreamTrack
import org.webrtc.VideoTrack as AndroidVideoTrack

actual open class MediaStreamTrack internal constructor(
    val android: AndroidMediaStreamTrack,
    private val mediaSource: MediaSource?,
) {

    protected val scope = MainScope()

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
                scope.launch { onUnmuteInternal.emit(Unit) }
            } else {
                scope.launch { onMuteInternal.emit(Unit) }
            }
        }

    actual val readyState: MediaStreamTrackState
        get() = android.state().asCommon()

    private val onEndedInternal = MutableSharedFlow<Unit>()
    actual val onEnded: Flow<Unit> = onEndedInternal.asSharedFlow()

    private val onMuteInternal = MutableSharedFlow<Unit>()
    actual val onMute: Flow<Unit> = onMuteInternal.asSharedFlow()

    private val onUnmuteInternal = MutableSharedFlow<Unit>()
    actual val onUnmute: Flow<Unit> = onUnmuteInternal.asSharedFlow()

    actual open fun stop() {
        if (readyState == MediaStreamTrackState.Ended) return
        scope.launch {
            onEndedInternal.emit(Unit)
            scope.cancel()
        }
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
