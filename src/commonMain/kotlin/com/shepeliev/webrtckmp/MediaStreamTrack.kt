package com.shepeliev.webrtckmp

import kotlinx.coroutines.flow.Flow

interface MediaStreamTrack {

    companion object {
        const val AUDIO_TRACK_KIND = "audio"
        const val VIDEO_TRACK_KIND = "video"
    }

    val id: String
    val kind: String
    var enabled: Boolean
    val state: State
    val onStop: Flow<MediaStreamTrack>

    fun stop()

    enum class State { Live, Ended }

    enum class MediaType { Audio, Video, Data, Unsupported; }
}

