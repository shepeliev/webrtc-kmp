package com.shepeliev.webrtckmp

import org.webrtc.AudioTrack
import org.webrtc.MediaStream
import org.webrtc.VideoTrack
import java.util.UUID

actual class MediaStream internal constructor(
    val android: MediaStream?,
    actual val id: String = android?.id ?: UUID.randomUUID().toString(),
) {

    private val _tracks = mutableListOf<MediaStreamTrack>()
    actual val tracks: List<MediaStreamTrack> = _tracks

    actual fun addTrack(track: MediaStreamTrack) {
        require(track is MediaStreamTrackImpl)

        android?.let {
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

        android?.let {
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
        android?.dispose()
    }
}
