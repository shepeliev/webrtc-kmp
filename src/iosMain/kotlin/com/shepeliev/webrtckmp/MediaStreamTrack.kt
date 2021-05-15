package com.shepeliev.webrtckmp

import WebRTC.RTCAudioTrack
import WebRTC.RTCMediaStreamTrack
import WebRTC.RTCMediaStreamTrackState
import WebRTC.RTCVideoTrack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlin.native.concurrent.AtomicInt

actual open class MediaStreamTrack(
    val native: RTCMediaStreamTrack,
    actual val remote: Boolean,
) {

    actual val id: String
        get() = native.trackId

    actual val kind: MediaStreamTrackKind
        get() = when (native.kind()) {
            "audio" -> MediaStreamTrackKind.Audio
            "video" -> MediaStreamTrackKind.Video
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
        get() = native.isEnabled
        set(value) {
            native.isEnabled = value
            if (value) {
                WebRtcKmp.mainScope.launch {  onUnmuteInternal.emit(Unit) }
            } else {
                WebRtcKmp.mainScope.launch {  onMuteInternal.emit(Unit) }
            }
        }

    actual val readOnly: Boolean = true

    actual val readyState: MediaStreamTrackState
        get() {
            if (endedFlag.value == 1) {
                return MediaStreamTrackState.Ended
            }
            return rtcMediaStreamTrackStateAsCommon(native.readyState)
        }

    private val onEndedInternal = MutableSharedFlow<Unit>()
    actual val onEnded: Flow<Unit> = onEndedInternal.asSharedFlow()

    private val onMuteInternal = MutableSharedFlow<Unit>()
    actual val onMute: Flow<Unit> = onEndedInternal.asSharedFlow()

    private val onUnmuteInternal = MutableSharedFlow<Unit>()
    actual val onUnmute: Flow<Unit> = onEndedInternal.asSharedFlow()

    private val endedFlag = AtomicInt(0)

    actual fun stop() {
        if (!endedFlag.compareAndSet(0, 1)) return
        enabled = false
        WebRtcKmp.mainScope.launch { onEndedInternal.emit(Unit) }
    }

    companion object {
        fun createCommon(native: RTCMediaStreamTrack, remote: Boolean): MediaStreamTrack {
            return when (native) {
                is RTCAudioTrack -> AudioStreamTrack(native, remote)
                is RTCVideoTrack -> VideoStreamTrack(native, remote)
                else -> error("Unknown native MediaStreamTrack: $this")
            }
        }
    }
}

private fun rtcMediaStreamTrackStateAsCommon(state: RTCMediaStreamTrackState): MediaStreamTrackState {
    return when (state) {
        RTCMediaStreamTrackState.RTCMediaStreamTrackStateLive -> MediaStreamTrackState.Live
        RTCMediaStreamTrackState.RTCMediaStreamTrackStateEnded -> MediaStreamTrackState.Ended
    }
}
