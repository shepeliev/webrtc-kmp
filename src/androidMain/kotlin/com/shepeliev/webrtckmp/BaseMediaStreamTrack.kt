package com.shepeliev.webrtckmp

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.webrtc.AudioTrack as NativeAudioTrack
import org.webrtc.MediaStreamTrack as NativeMediaStreamTrack
import org.webrtc.VideoTrack as NativeVideoTrack

abstract class BaseMediaStreamTrack : MediaStreamTrack {
    abstract val native: NativeMediaStreamTrack

    override val id: String
        get() = native.id()

    override val kind: String
        get() = native.kind()

    override var enabled: Boolean
        get() = native.enabled()
        set(value) {
            native.setEnabled(value)
        }

    override val state: MediaStreamTrack.State
        get() = native.state().asCommon()

    private val onStopInternal = MutableSharedFlow<BaseMediaStreamTrack>()
    override val onStop = onStopInternal.asSharedFlow()

    private var isStopped = false

    override fun stop() {
        if (isStopped) return
        isStopped = true
        enabled = false
        native.dispose()
        WebRtcKmp.mainScope.launch { onStopInternal.emit(this@BaseMediaStreamTrack) }
    }
}

internal fun NativeMediaStreamTrack.asCommon(): MediaStreamTrack {
    return when (this) {
        is NativeAudioTrack -> AudioTrack(this)
        is NativeVideoTrack -> VideoTrack(this)
        else -> error("Unknown native MediaStreamTrack: $this")
    }
}

private fun NativeMediaStreamTrack.State.asCommon(): MediaStreamTrack.State {
    return when (this) {
        org.webrtc.MediaStreamTrack.State.LIVE -> MediaStreamTrack.State.Live
        org.webrtc.MediaStreamTrack.State.ENDED -> MediaStreamTrack.State.Ended
    }
}
