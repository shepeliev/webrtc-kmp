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
        get() = native.state().asCommon()

    override fun dispose() {
        native.dispose()
    }
}

fun MediaStreamTrack.MediaType.asNative(): NativeMediaStreamTrack.MediaType {
    return when(this) {
        MediaStreamTrack.MediaType.Audio -> {
            NativeMediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO
        }

        MediaStreamTrack.MediaType.Video -> {
            NativeMediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO
        }

        MediaStreamTrack.MediaType.Data -> {
            error("Android doesn't define data media type")
        }
    }
}

fun NativeMediaStreamTrack.MediaType.asCommon() : MediaStreamTrack.MediaType {
    return when(this) {
        NativeMediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO -> {
            MediaStreamTrack.MediaType.Audio
        }

        NativeMediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO -> {
            MediaStreamTrack.MediaType.Video
        }
    }
}

fun NativeMediaStreamTrack.asCommon(): MediaStreamTrack {
    return when (this) {
        is org.webrtc.AudioTrack -> AudioTrack(this)
        is org.webrtc.VideoTrack -> VideoTrack(this)
        else -> error("Unknown native MediaStreamTrack: $this")
    }
}

private fun NativeMediaStreamTrack.State.asCommon(): MediaStreamTrack.State {
    return when(this) {
        org.webrtc.MediaStreamTrack.State.LIVE -> MediaStreamTrack.State.Live
        org.webrtc.MediaStreamTrack.State.ENDED -> MediaStreamTrack.State.Ended
    }
}
