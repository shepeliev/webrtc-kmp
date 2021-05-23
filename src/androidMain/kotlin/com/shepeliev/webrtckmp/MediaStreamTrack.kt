package com.shepeliev.webrtckmp

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.webrtc.AudioTrack as AndroidAudioTrack
import org.webrtc.MediaStreamTrack as AndroidMediaStreamTrack
import org.webrtc.VideoTrack as AndroidVideoTrack

actual open class MediaStreamTrack internal constructor(
    val native: AndroidMediaStreamTrack,
    actual val remote: Boolean,
) {

    internal val scope = MainScope()

    actual val id: String
        get() = native.id()

    actual val kind: MediaStreamTrackKind
        get() = when (native.kind()) {
            AndroidMediaStreamTrack.AUDIO_TRACK_KIND -> MediaStreamTrackKind.Audio
            AndroidMediaStreamTrack.VIDEO_TRACK_KIND -> MediaStreamTrackKind.Video
            else -> throw IllegalStateException("Unknown track kind: ${native.kind()}")
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
        get() = native.enabled()
        set(value) {
            native.setEnabled(value)
            if (value) {
                scope.launch { onUnmuteInternal.emit(Unit) }
            } else {
                scope.launch { onMuteInternal.emit(Unit) }
            }
        }

    actual val readOnly: Boolean = true

    actual val readyState: MediaStreamTrackState
        get() = native.state().asCommon()

    private val onEndedInternal = MutableSharedFlow<Unit>()
    actual val onEnded: Flow<Unit> = onEndedInternal.asSharedFlow()

    private val onMuteInternal = MutableSharedFlow<Unit>()
    actual val onMute: Flow<Unit> = onEndedInternal.asSharedFlow()

    private val onUnmuteInternal = MutableSharedFlow<Unit>()
    actual val onUnmute: Flow<Unit> = onEndedInternal.asSharedFlow()

    actual fun stop() {
        if (readyState == MediaStreamTrackState.Ended) return

        when(kind) {
            MediaStreamTrackKind.Audio -> PhoneMediaDevices.onAudioTrackStopped()
            MediaStreamTrackKind.Video -> PhoneMediaDevices.onVideoTrackStopped()
        }

        native.dispose()
        scope.launch {
            onEndedInternal.emit(Unit)
            scope.cancel()
        }
    }
}

internal fun AndroidMediaStreamTrack.asCommon(remote: Boolean): MediaStreamTrack {
    return when (this) {
        is AndroidAudioTrack -> AudioStreamTrack(this, remote)
        is AndroidVideoTrack -> VideoStreamTrack(this, remote)
        else -> error("Unknown native MediaStreamTrack: $this")
    }
}

private fun AndroidMediaStreamTrack.State.asCommon(): MediaStreamTrackState {
    return when (this) {
        org.webrtc.MediaStreamTrack.State.LIVE -> MediaStreamTrackState.Live
        org.webrtc.MediaStreamTrack.State.ENDED -> MediaStreamTrackState.Ended
    }
}
