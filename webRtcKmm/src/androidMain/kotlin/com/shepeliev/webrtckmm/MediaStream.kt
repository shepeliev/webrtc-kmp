package com.shepeliev.webrtckmm

import org.webrtc.MediaStream as NativeMediaStream

actual class MediaStream internal constructor(val native: NativeMediaStream) {
    actual val id: String
        get() = native.id

    actual fun addTrack(track: AudioTrack): Boolean = native.addTrack(track.native)
    actual fun addTrack(track: VideoTrack): Boolean = native.addTrack(track.native)

    actual fun addPreservedTrack(track: VideoTrack): Boolean {
        return native.addPreservedTrack(track.native)
    }

    actual fun removeTrack(track: AudioTrack): Boolean = native.removeTrack(track.native)
    actual fun removeTrack(track: VideoTrack): Boolean = native.removeTrack(track.native)
    actual override fun toString(): String = native.toString()
}
