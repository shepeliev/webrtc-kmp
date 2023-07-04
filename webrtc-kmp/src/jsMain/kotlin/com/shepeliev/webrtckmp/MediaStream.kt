package com.shepeliev.webrtckmp

import org.w3c.dom.mediacapture.MediaStream as JsMediaStream

actual class MediaStream internal constructor(val js: JsMediaStream) {

    actual val id: String
        get() = js.id

    actual val tracks: List<MediaStreamTrack>
        get() = js.getTracks().map { it.asCommon() }

    actual fun addTrack(track: MediaStreamTrack) {
        require(track is MediaStreamTrackImpl)
        js.addTrack(track.js)
    }

    actual fun getTrackById(id: String): MediaStreamTrack? = js.getTrackById(id)?.asCommon()

    actual fun removeTrack(track: MediaStreamTrack) {
        require(track is MediaStreamTrackImpl)
        js.removeTrack(track.js)
    }

    actual fun release() {
        tracks.forEach(MediaStreamTrack::stop)
    }
}
