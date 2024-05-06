package com.shepeliev.webrtckmp

import com.shepeliev.webrtckmp.externals.PlatformMediaStream
import com.shepeliev.webrtckmp.externals.getTracks
import com.shepeliev.webrtckmp.internal.AudioTrackImpl
import com.shepeliev.webrtckmp.internal.VideoTrackImpl

actual class MediaStream internal constructor(val js: PlatformMediaStream) {
    actual constructor() : this(PlatformMediaStream())

    actual val id: String get() = js.id
    actual val tracks: List<MediaStreamTrack> get() = js.getTracks().map {
        when (it.kind) {
            "audio" -> AudioTrackImpl(it)
            "video" -> VideoTrackImpl(it)
            else -> throw IllegalArgumentException("Unknown track kind: ${it.kind}")
        }
    }

    actual fun addTrack(track: MediaStreamTrack) {
        require(track is MediaStreamTrackImpl)
        js.addTrack(track.platform)
    }

    actual fun getTrackById(id: String): MediaStreamTrack? = js.getTrackById(id)?.let { MediaStreamTrackImpl(it) }

    actual fun removeTrack(track: MediaStreamTrack) {
        require(track is MediaStreamTrackImpl)
        js.removeTrack(track.platform)
    }

    actual fun release() {
        tracks.forEach(MediaStreamTrack::stop)
    }
}
