package com.shepeliev.webrtckmp

import org.webrtc.AudioTrack
import org.webrtc.MediaStream
import org.webrtc.VideoTrack

actual class MediaStream internal constructor(val android: MediaStream) {
    actual val id: String
        get() = android.id

    actual val tracks: List<MediaStreamTrack>
        get() = audioTracks + videoTracks

    private var audioTracksInternal = mutableListOf<AudioStreamTrack>()
    actual val audioTracks: List<AudioStreamTrack> = audioTracksInternal

    private var videoTracksInternal = mutableListOf<VideoStreamTrack>()
    actual val videoTracks: List<VideoStreamTrack> = videoTracksInternal

    actual fun addTrack(track: AudioStreamTrack) {
        android.addTrack(track.android as AudioTrack)
        audioTracksInternal += track
    }

    actual fun addTrack(track: VideoStreamTrack) {
        android.addTrack(track.android as VideoTrack)
        videoTracksInternal += track
    }

    actual fun getTrackById(id: String): MediaStreamTrack? {
        return tracks.firstOrNull { it.id == id }
    }

    actual fun removeTrack(track: AudioStreamTrack) {
        android.removeTrack(track.android as AudioTrack)
        audioTracksInternal -= track
    }

    actual fun removeTrack(track: VideoStreamTrack) {
        android.removeTrack(track.android as VideoTrack)
        videoTracksInternal -= track
    }

    actual fun release() {
        tracks.forEach(MediaStreamTrack::stop)
        android.dispose()
    }
}
