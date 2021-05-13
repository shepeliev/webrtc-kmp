package com.shepeliev.webrtckmp

import WebRTC.RTCAudioTrack
import WebRTC.RTCMediaStreamTrack
import WebRTC.RTCMediaStreamTrackState
import WebRTC.RTCRtpMediaType
import WebRTC.RTCRtpMediaType.RTCRtpMediaTypeAudio
import WebRTC.RTCRtpMediaType.RTCRtpMediaTypeData
import WebRTC.RTCRtpMediaType.RTCRtpMediaTypeUnsupported
import WebRTC.RTCRtpMediaType.RTCRtpMediaTypeVideo
import WebRTC.RTCVideoTrack

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

internal fun rtcRtpMediaTypeAsCommon(type: RTCRtpMediaType): MediaStreamTrack.MediaType {
    return when (type) {
        RTCRtpMediaTypeAudio -> MediaStreamTrack.MediaType.Audio
        RTCRtpMediaTypeVideo -> MediaStreamTrack.MediaType.Video
        RTCRtpMediaTypeData -> MediaStreamTrack.MediaType.Data
        RTCRtpMediaTypeUnsupported -> MediaStreamTrack.MediaType.Unsupported
    }
}

private fun rtcMediaStreamTrackStateAsCommon(state: RTCMediaStreamTrackState): MediaStreamTrack.State {
    return when (state) {
        RTCMediaStreamTrackState.RTCMediaStreamTrackStateLive -> MediaStreamTrack.State.Live
        RTCMediaStreamTrackState.RTCMediaStreamTrackStateEnded -> MediaStreamTrack.State.Ended
    }
}
