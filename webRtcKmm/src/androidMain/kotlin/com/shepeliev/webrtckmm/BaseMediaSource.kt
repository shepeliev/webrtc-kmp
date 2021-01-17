package com.shepeliev.webrtckmm

import org.webrtc.MediaSource as NativeMediaSource

abstract class BaseMediaSource(): MediaSource {

    abstract val native: NativeMediaSource

    override val state: MediaSource.State
        get() = native.state().toCommon()

    override fun dispose() {
        native.dispose()
    }

    private fun NativeMediaSource.State.toCommon(): MediaSource.State {
        return when(this) {
            org.webrtc.MediaSource.State.INITIALIZING -> MediaSource.State.Initializing
            org.webrtc.MediaSource.State.LIVE -> MediaSource.State.Live
            org.webrtc.MediaSource.State.ENDED -> MediaSource.State.Ended
            org.webrtc.MediaSource.State.MUTED -> MediaSource.State.Muted
        }
    }
}
