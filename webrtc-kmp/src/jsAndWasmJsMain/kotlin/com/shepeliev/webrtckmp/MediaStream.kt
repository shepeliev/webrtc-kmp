package com.shepeliev.webrtckmp

import com.shepeliev.webrtckmp.externals.PlatformMediaStream
import com.shepeliev.webrtckmp.externals.getTracks

actual class MediaStream internal constructor(internal val js: PlatformMediaStream) {
    actual val id: String get() = js.id
    actual val tracks: List<MediaStreamTrack> get() = js.getTracks().map { MediaStreamTrackImpl(it) }

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
