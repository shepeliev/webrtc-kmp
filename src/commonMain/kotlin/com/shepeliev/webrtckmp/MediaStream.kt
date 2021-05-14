package com.shepeliev.webrtckmp

import com.shepeliev.webrtckmp.utils.uuid
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class MediaStream internal constructor(val id: String = uuid()) {
    val tracks: List<MediaStreamTrack>
        get() = tracksInternal.values.flatten().toList()

    val audioTracks: List<AudioTrack>
        get() = tracks
            .filter { it.kind == MediaStreamTrack.AUDIO_TRACK_KIND }
            .map { it as AudioTrack }

    val videoTracks: List<VideoTrack>
        get() = tracks
            .filter { it.kind == MediaStreamTrack.VIDEO_TRACK_KIND }
            .map { it as VideoTrack }

    private val onAddTrackInternal = MutableSharedFlow<MediaStreamTrack>()
    val onAddTrack = onAddTrackInternal.asSharedFlow()

    private val onRemoveTrackInternal = MutableSharedFlow<MediaStreamTrack>()
    val onRemoveTrack = onRemoveTrackInternal.asSharedFlow()

    private val tracksInternal = mutableMapOf<String, MutableList<MediaStreamTrack>>()

    fun addTrack(track: MediaStreamTrack) {
        val trackList = tracksInternal.getOrPut(track.id) { mutableListOf() }
        trackList += track
        WebRtcKmp.mainScope.launch { onAddTrackInternal.emit(track) }
    }

    fun getTrackById(id: String): MediaStreamTrack? = tracksInternal[id]?.firstOrNull()

    fun removeTrack(track: MediaStreamTrack) {
        val trackList = tracksInternal.remove(track.id) ?: return
        WebRtcKmp.mainScope.launch {
            trackList.forEach { onRemoveTrackInternal.emit(it) }
        }
    }
}
