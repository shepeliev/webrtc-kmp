package com.shepeliev.webrtckmp

import org.w3c.dom.mediacapture.MediaStream as JsMediaStream

actual class MediaStream internal constructor(val js: JsMediaStream) {

    actual constructor(tracks: List<MediaStreamTrack>) : this(JsMediaStream(tracks.map { it.js }.toTypedArray()))

    actual val id: String
        get() = js.id

    actual val tracks: List<MediaStreamTrack>
        get() = audioTracks + videoTracks

    actual fun addTrack(track: MediaStreamTrack) {
        js.addTrack(track.js)
    }

    actual fun getTrackById(id: String): MediaStreamTrack? = js.getTrackById(id)?.asCommon()

    actual fun removeTrack(track: MediaStreamTrack) {
        js.removeTrack(track.js)
    }

    actual fun release() {
        tracks.forEach(MediaStreamTrack::stop)
    }
}
