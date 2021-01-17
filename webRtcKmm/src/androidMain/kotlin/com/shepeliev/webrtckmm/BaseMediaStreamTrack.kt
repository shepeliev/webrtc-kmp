package com.shepeliev.webrtckmm

import org.webrtc.MediaStreamTrack as NativeMediaStreamTrack

abstract class BaseMediaStreamTrack : MediaStreamTrack {
    abstract val native: NativeMediaStreamTrack

    override val id: String
        get() = native.id()

    override val kind: String
        get() = native.kind()

    override var enabled: Boolean
        get() = native.enabled()
        set(value) { native.setEnabled(value) }

    override val state: MediaStreamTrack.State
        get() = native.state().toCommon()

    override fun dispose() {
        native.dispose()
    }
}

fun MediaStreamTrack.MediaType.toNative(): NativeMediaStreamTrack.MediaType {
    return when(this) {
        MediaStreamTrack.MediaType.MediaTypeAudio -> {
            NativeMediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO
        }

        MediaStreamTrack.MediaType.MediaTypeVideo -> {
            NativeMediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO
        }
    }
}

fun NativeMediaStreamTrack.MediaType.toCommon() : MediaStreamTrack.MediaType {
    return when(this) {
        NativeMediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO -> {
            MediaStreamTrack.MediaType.MediaTypeAudio
        }

        NativeMediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO -> {
            MediaStreamTrack.MediaType.MediaTypeVideo
        }
    }
}

fun NativeMediaStreamTrack.toCommon(): MediaStreamTrack {
    return when (this) {
        is org.webrtc.AudioTrack -> AudioTrack(this)
        is org.webrtc.VideoTrack -> VideoTrack(this)
        else -> error("Unknown native MediaStreamTrack: $this")
    }
}

private fun NativeMediaStreamTrack.State.toCommon(): MediaStreamTrack.State {
    return when(this) {
        org.webrtc.MediaStreamTrack.State.LIVE -> MediaStreamTrack.State.Live
        org.webrtc.MediaStreamTrack.State.ENDED -> MediaStreamTrack.State.Ended
    }
}
