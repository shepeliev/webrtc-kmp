package com.shepeliev.webrtckmp

import org.w3c.dom.mediacapture.MediaStream as JsMediaStream

internal class MediaStreamImpl(val native: JsMediaStream) : MediaStream {
    override val id: String get() = native.id
    override val tracks: List<MediaStreamTrack> get() = native.getTracks().map { it.asCommon() }

    override fun addTrack(track: MediaStreamTrack) {
        require(track is MediaStreamTrackImpl)
        native.addTrack(track.native)
    }

    override fun getTrackById(id: String): MediaStreamTrack? = native.getTrackById(id)?.asCommon()

    override fun removeTrack(track: MediaStreamTrack) {
        require(track is MediaStreamTrackImpl)
        native.removeTrack(track.native)
    }
}

actual fun MediaStream(tracks: List<MediaStreamTrack>, id: String): MediaStream =
    MediaStreamImpl(JsMediaStream(tracks.map { (it as MediaStreamTrackImpl).native }.toTypedArray()))

fun MediaStream(js: JsMediaStream): MediaStream = MediaStreamImpl(js)

val MediaStream.native: JsMediaStream get() = (this as MediaStreamImpl).native

@Deprecated("Use MediaStream.native instead", ReplaceWith("native"))
val MediaStream.js: JsMediaStream get() = (this as MediaStreamImpl).native
