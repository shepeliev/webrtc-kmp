package com.shepeliev.webrtckmm

import cocoapods.GoogleWebRTC.RTCAudioTrack
import cocoapods.GoogleWebRTC.RTCMediaStream
import cocoapods.GoogleWebRTC.RTCVideoTrack

actual class MediaStream internal constructor(val native: RTCMediaStream) {
    actual val id: String
        get() = native.streamId

    actual val audioTracks: List<AudioTrack>
        get() = native.audioTracks.map { AudioTrack(it as RTCAudioTrack) }

    actual val videoTracks: List<VideoTrack>
        get() = native.videoTracks.map { VideoTrack(it as RTCVideoTrack) }

    actual fun addTrack(track: AudioTrack): Boolean {
        native.addAudioTrack(track.native)
        return true
    }

    actual fun addTrack(track: VideoTrack): Boolean {
        native.addVideoTrack(track.native)
        return true
    }

    actual fun addPreservedTrack(track: VideoTrack): Boolean {
        native.addVideoTrack(track.native)
        return true
    }

    actual fun removeTrack(track: AudioTrack): Boolean {
        native.removeAudioTrack(track.native)
        return true
    }

    actual fun removeTrack(track: VideoTrack): Boolean {
        native.removeVideoTrack(track.native)
        return true
    }

    actual override fun toString(): String = native.toString()
}
