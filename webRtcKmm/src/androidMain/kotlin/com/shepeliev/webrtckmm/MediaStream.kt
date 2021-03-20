package com.shepeliev.webrtckmm

import org.webrtc.MediaStream as NativeMediaStream

actual class MediaStream internal constructor(val native: NativeMediaStream) : VideoStream {
    actual val id: String
        get() = native.id

    actual val audioTracks: List<AudioTrack>
        get() = native.audioTracks.map { AudioTrack(it) }

    actual val videoTracks: List<VideoTrack>
        get() = native.videoTracks.map { VideoTrack(it) }

    actual fun addTrack(audioTrack: AudioTrack): Boolean {
        return native.addTrack(audioTrack.native)
    }

    actual fun addTrack(videoTrack: VideoTrack): Boolean {
        return native.addTrack(videoTrack.native)
    }

    actual fun removeTrack(audioTrack: AudioTrack): Boolean {
        return native.removeTrack(audioTrack.native)
    }

    actual fun removeTrack(videoTrack: VideoTrack): Boolean {
        return native.removeTrack(videoTrack.native)
    }

    actual override fun videoTrack(): VideoTrack? = videoTracks.firstOrNull()
}