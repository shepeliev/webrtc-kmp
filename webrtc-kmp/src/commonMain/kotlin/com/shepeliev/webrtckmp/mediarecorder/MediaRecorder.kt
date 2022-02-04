package com.shepeliev.webrtckmp.mediarecorder

import com.shepeliev.webrtckmp.MediaStream
import kotlinx.coroutines.flow.Flow

expect class MediaRecorder(stream: MediaStream, options: MediaRecorderOptions = MediaRecorderOptions()) {
    val onDataAvailable: Flow<String>
    val onError: Flow<Throwable>
    val onPause: Flow<Unit>
    val onResume: Flow<Unit>
    val onStart: Flow<Unit>
    val onStop: Flow<Unit>
    val state: MediaRecorderState

    fun pause()
    fun requestData()
    fun resume()
    fun start(timeSliceMillis: Long = -1)
    fun stop()
}

data class MediaRecorderOptions(
    val audioBitsPerSeconds: Int = DEFAULT_AUDIO_BITS_PER_SECOND,
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

enum class MediaRecorderState { Inactive, Recording, Paused }

private const val DEFAULT_AUDIO_BITS_PER_SECOND = 128_000
private const val DEFAULT_VIDEO_BITS_PER_SECOND = 2_500_000
