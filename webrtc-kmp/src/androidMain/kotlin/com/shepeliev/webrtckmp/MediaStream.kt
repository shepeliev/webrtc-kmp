package com.shepeliev.webrtckmp

import org.webrtc.AudioTrack
import org.webrtc.MediaStream
import org.webrtc.VideoTrack
import java.util.UUID

actual class MediaStream internal constructor(val android: MediaStream) {
    actual constructor() : this(WebRtc.peerConnectionFactory.createLocalMediaStream(UUID.randomUUID().toString()))

    actual val id: String = android.id
    private val _tracks = mutableListOf<MediaStreamTrack>()
    actual val tracks: List<MediaStreamTrack> = _tracks

    actual fun addTrack(track: MediaStreamTrack) {
        require(track is MediaStreamTrackImpl)

        android.let {
            when (track.android) {
                is AudioTrack -> it.addTrack(track.android)
                is VideoTrack -> it.addTrack(track.android)
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

        android.let {
            when (track.android) {
                is AudioTrack -> it.removeTrack(track.android)
                is VideoTrack -> it.removeTrack(track.android)
                else -> error("Unknown MediaStreamTrack kind: ${track.kind}")
            }
        }
        _tracks -= track
    }

    actual fun release() {
        tracks.forEach(MediaStreamTrack::stop)
        android.dispose()
    }
}
