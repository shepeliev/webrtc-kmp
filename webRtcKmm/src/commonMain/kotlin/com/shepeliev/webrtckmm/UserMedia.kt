package com.shepeliev.webrtckmm

data class UserMedia(
    val audioTracks: List<AudioTrack>,
    val videoTracks: List<VideoTrack>,
)
