package com.shepeliev.webrtckmm

import cocoapods.GoogleWebRTC.RTCAudioTrack
import cocoapods.GoogleWebRTC.RTCMediaStream
import cocoapods.GoogleWebRTC.RTCVideoTrack

actual class MediaStream internal constructor(val native: RTCMediaStream) : VideoStream {
    actual val id: String
        get() = native.streamId

    actual val audioTracks: List<AudioTrack>
        get() = native.audioTracks.map { AudioTrack(it as RTCAudioTrack) }

    actual val videoTracks: List<VideoTrack>
        get() = native.videoTracks.map { VideoTrack(it as RTCVideoTrack) }

    actual fun addTrack(audioTrack: AudioTrack): Boolean {
        native.addAudioTrack(audioTrack.native)
        return true
    }

    actual fun addTrack(videoTrack: VideoTrack): Boolean {
        native.addVideoTrack(videoTrack.native)
        return true
    }

    actual fun removeTrack(audioTrack: AudioTrack): Boolean {
        native.removeAudioTrack(audioTrack.native)
        return true
    }

    actual fun removeTrack(videoTrack: VideoTrack): Boolean {
        native.removeVideoTrack(videoTrack.native)
        return true
    }

    actual override fun videoTrack(): VideoTrack? = videoTracks.firstOrNull()
}