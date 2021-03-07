package com.shepeliev.webrtckmm

import cocoapods.GoogleWebRTC.RTCAudioTrack
import cocoapods.GoogleWebRTC.RTCMediaStreamTrack
import cocoapods.GoogleWebRTC.RTCMediaStreamTrackState
import cocoapods.GoogleWebRTC.RTCRtpMediaType
import cocoapods.GoogleWebRTC.RTCVideoTrack

abstract class BaseMediaStreamTrack : MediaStreamTrack {
    abstract val native: RTCMediaStreamTrack

    override val id: String
        get() = native.trackId

    override val kind: String
        get() = native.kind

    override var enabled: Boolean
        get() = native.isEnabled
        set(value) {
            native.isEnabled = value
        }

    override val state: MediaStreamTrack.State
        get() = rtcMediaStreamTrackStateAsCommon(native.readyState)

    override fun stop() {
        // not applicable
    }

    companion object {
        fun createCommon(native: RTCMediaStreamTrack): MediaStreamTrack {
            return when (native) {
                is RTCAudioTrack -> AudioTrack(native)
                is RTCVideoTrack -> VideoTrack(native)
                else -> error("Unknown native MediaStreamTrack: $this")
            }
        }
    }
}

internal fun MediaStreamTrack.MediaType.asNative(): RTCRtpMediaType {
    return when (this) {
        MediaStreamTrack.MediaType.Audio -> RTCRtpMediaType.RTCRtpMediaTypeAudio
        MediaStreamTrack.MediaType.Video -> RTCRtpMediaType.RTCRtpMediaTypeVideo
        MediaStreamTrack.MediaType.Data -> RTCRtpMediaType.RTCRtpMediaTypeData
    }
}

internal fun rtcRtpMediaTypeAsCommon(type: RTCRtpMediaType): MediaStreamTrack.MediaType {
    return when (type) {
        RTCRtpMediaType.RTCRtpMediaTypeAudio -> {
            MediaStreamTrack.MediaType.Audio
        }

        RTCRtpMediaType.RTCRtpMediaTypeVideo -> {
            MediaStreamTrack.MediaType.Video
        }

        RTCRtpMediaType.RTCRtpMediaTypeData -> {
            MediaStreamTrack.MediaType.Data
        }
    }
}

internal fun RTCMediaStreamTrack.asCommon(): MediaStreamTrack {
    return when (this) {
        is RTCAudioTrack -> AudioTrack(this)
        is RTCVideoTrack -> VideoTrack(this)
        else -> error("Unknown native MediaStreamTrack: $this")
    }
}

private fun rtcMediaStreamTrackStateAsCommon(state: RTCMediaStreamTrackState): MediaStreamTrack.State {
    return when (state) {
        RTCMediaStreamTrackState.RTCMediaStreamTrackStateLive -> MediaStreamTrack.State.Live
        RTCMediaStreamTrackState.RTCMediaStreamTrackStateEnded -> MediaStreamTrack.State.Ended
    }
}
