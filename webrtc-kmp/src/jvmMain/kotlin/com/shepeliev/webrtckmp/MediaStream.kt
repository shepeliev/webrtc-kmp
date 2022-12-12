package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.media.audio.AudioTrack
import dev.onvoid.webrtc.media.video.VideoTrack
import dev.onvoid.webrtc.media.MediaStream as NativeMediaStream
import java.util.UUID

actual class MediaStream internal constructor(
    val native: NativeMediaStream? = null,
    actual val id: String = native?.id() ?: UUID.randomUUID().toString(),
) {

    private val _tracks = mutableListOf<MediaStreamTrack>()
    actual val tracks: List<MediaStreamTrack> = _tracks

    actual open fun addTrack(track: MediaStreamTrack) {
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

    actual open fun getTrackById(id: String): MediaStreamTrack? {
        return tracks.firstOrNull { it.id == id }
    }

    actual open fun removeTrack(track: MediaStreamTrack) {
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

    actual open fun release() {
        tracks.forEach(MediaStreamTrack::stop)
        native?.dispose()
    }
}
