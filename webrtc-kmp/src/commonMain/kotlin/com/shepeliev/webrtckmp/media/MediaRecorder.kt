package com.shepeliev.webrtckmp.media

import com.shepeliev.webrtckmp.MediaStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Used to record audio and video from the [MediaStream].
 */
expect class MediaRecorder(stream: MediaStream, options: MediaRecorderOptions = MediaRecorderOptions()) {

    /**
     * [MediaRecorder] state
     */
    val state: StateFlow<MediaRecorderState>

    /**
     * Emits path to the recorded file when it's available
     */
    val onDataAvailable: Flow<String>

    /**
     * Start recording
     */
    suspend fun start()

    /**
     * Stop recording
     */
    fun stop()
}

sealed interface MediaRecorderState {
    object Inactive : MediaRecorderState
    object Recording : MediaRecorderState
    object Stopping : MediaRecorderState
    data class Failed(val error: Throwable) : MediaRecorderState
}

data class MediaRecorderOptions(
    val audioBitsPerSeconds: Int = DEFAULT_AUDIO_BITS_PER_SECOND,
    val videoFrameRate: Int = DEFAULT_VIDEO_FRAME_RATE,
    val videoBitsPerSeconds: Int = DEFAULT_VIDEO_BITS_PER_SECOND,
    val mediaFormat: MediaFormat = MediaFormat(),
)

data class MediaFormat(
    val mimeType: MimeType = MimeType.VideoMp4,
    val audioCodec: AudioCodec = AudioCodec.AAC,
    val videoCodec: VideoCodec = VideoCodec.AVC,
)

enum class MimeType { VideoMp4 }

enum class AudioCodec { AAC }

enum class VideoCodec { AVC }

private const val DEFAULT_AUDIO_BITS_PER_SECOND = 128_000
private const val DEFAULT_VIDEO_FRAME_RATE = 30
private const val DEFAULT_VIDEO_BITS_PER_SECOND = 2_500_000
