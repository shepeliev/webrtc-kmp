package com.shepeliev.webrtckmm

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

    override fun stop() {
        enabled = false

        when (this.kind) {
            MediaStreamTrack.AUDIO_TRACK_KIND -> {
                MediaDevices.onAudioTrackStopped(id)
            }
            MediaStreamTrack.VIDEO_TRACK_KIND -> {
                MediaDevices.onVideoTrackStopped(id)
            }
        }
    }
}

fun MediaStreamTrack.MediaType.asNative(): NativeMediaStreamTrack.MediaType {
    return when (this) {
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

fun NativeMediaStreamTrack.MediaType.asCommon(): MediaStreamTrack.MediaType {
    return when (this) {
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
