package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.media.audio.AudioTrack
import dev.onvoid.webrtc.media.video.VideoTrack
import java.util.UUID
import dev.onvoid.webrtc.media.MediaStream as NativeMediaStream

actual class MediaStream internal constructor(
    val native: NativeMediaStream? = null,
    actual val id: String = native?.id() ?: UUID.randomUUID().toString(),
) {

    actual constructor() : this(null)

    private val _tracks = mutableListOf<MediaStreamTrack>()
    actual val tracks: List<MediaStreamTrack> = _tracks

    actual fun addTrack(track: MediaStreamTrack) {
        require(track is MediaStreamTrackImpl)

        native?.let {
            when (track.native) {
                is AudioTrack -> it.addTrack(track.native)
                is VideoTrack -> it.addTrack(track.native)
                else -> error("Unknown MediaStreamTrack kind: ${track.kind}")
            }
        }
        _tracks += track
    }

    actual fun getTrackById(id: String): MediaStreamTrack? {
        return tracks.firstOrNull { it.id == id }
    }

    actual fun removeTrack(track: MediaStreamTrack) {
        require(track is MediaStreamTrackImpl)

        native?.let {
            when (track.native) {
                is AudioTrack -> it.removeTrack(track.native)
                is VideoTrack -> it.removeTrack(track.native)
                else -> error("Unknown MediaStreamTrack kind: ${track.kind}")
            }
        }
        _tracks -= track
    }

    actual fun release() {
        tracks.forEach(MediaStreamTrack::stop)
        native?.dispose()
    }
}
