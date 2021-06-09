package com.shepeliev.webrtckmp

import org.w3c.dom.mediacapture.MediaStream as JsMediaStream

actual class MediaStream internal constructor(val js: JsMediaStream) {
    actual val tracks: List<MediaStreamTrack>
        get() = audioTracks + videoTracks

    actual val audioTracks: List<AudioStreamTrack>
        get() = js.getVideoTracks().map { AudioStreamTrack(it) }

    actual val videoTracks: List<VideoStreamTrack>
        get() = js.getVideoTracks().map { VideoStreamTrack(it) }

    actual fun addTrack(track: AudioStreamTrack) {
        js.addTrack(track.js)
    }

    actual fun addTrack(track: VideoStreamTrack) {
        js.addTrack(track.js)
    }

    actual fun getTrackById(id: String): MediaStreamTrack? {
        return js.getTrackById(id)?.let {
            when (it.kind) {
                "audio" -> AudioStreamTrack(it)
                "video" -> VideoStreamTrack(it)
                else -> error("Unknown kind of media stream track: ${it.kind}")
            }
        }
    }

    actual fun removeTrack(track: AudioStreamTrack) {
        js.removeTrack(track.js)
    }

    actual fun removeTrack(track: VideoStreamTrack) {
        js.removeTrack(track.js)
    }

    actual fun dispose() {
        tracks.forEach(MediaStreamTrack::stop)
    }
}
