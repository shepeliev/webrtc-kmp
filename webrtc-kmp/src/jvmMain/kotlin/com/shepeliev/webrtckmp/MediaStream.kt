package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.media.audio.AudioTrack
import dev.onvoid.webrtc.media.video.VideoTrack
import java.util.UUID

actual class MediaStream internal constructor(
    val jvm: MediaStream?,
    actual val id: String = jvm?.id ?: UUID.randomUUID().toString(),
) {

    private val _tracks = mutableListOf<MediaStreamTrack>()
    actual val tracks: List<MediaStreamTrack> = _tracks

    actual fun addTrack(track: MediaStreamTrack) {
        jvm?.let {
            when (track.jvm.kind) {
                dev.onvoid.webrtc.media.MediaStreamTrack.AUDIO_TRACK_KIND -> it.addTrack(track)
                dev.onvoid.webrtc.media.MediaStreamTrack.VIDEO_TRACK_KIND -> it.addTrack(track)
                else -> error("Unknown MediaStreamTrack kind: ${track.kind}")
            }
        }
        _tracks += track
    }

    actual fun getTrackById(id: String): MediaStreamTrack? {
        return tracks.firstOrNull { it.id == id }
    }

    actual fun removeTrack(track: MediaStreamTrack) {
        jvm?.let {
            when (track.jvm) {
                is AudioTrack -> it.removeTrack(track)
                is VideoTrack -> it.removeTrack(track)
                else -> error("Unknown MediaStreamTrack kind: ${track.kind}")
            }
        }
        _tracks -= track
    }

    actual fun release() {
        tracks.forEach(MediaStreamTrack::stop)
        jvm?.release() // TODO: Confirm: Equivalent to Dispose ?
    }
}
