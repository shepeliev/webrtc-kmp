package com.shepeliev.webrtckmp

import com.shepeliev.webrtckmp.utils.uuid

class MediaStream internal constructor(val id: String = uuid()) {
    val tracks: List<MediaStreamTrack>
        get() = tracksInternal.values.flatten().toList()

    val audioTracks: List<AudioStreamTrack>
        get() = tracks
            .filter { it.kind == MediaStreamTrackKind.Audio }
            .map { it as AudioStreamTrack }

    val videoTracks: List<VideoStreamTrack>
        get() = tracks
            .filter { it.kind == MediaStreamTrackKind.Video }
            .map { it as VideoStreamTrack }

    private val tracksInternal = mutableMapOf<String, MutableList<MediaStreamTrack>>()

    fun addTrack(track: MediaStreamTrack) {
        val trackList = tracksInternal.getOrPut(track.id) { mutableListOf() }
        trackList += track
    }

    fun getTrackById(id: String): MediaStreamTrack? = tracksInternal[id]?.firstOrNull()

    fun removeTrack(track: MediaStreamTrack) {
        tracksInternal.remove(track.id)
    }
}
