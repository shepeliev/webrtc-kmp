@file:JvmName("AndroidMediaRecorder")

package com.shepeliev.webrtckmp.media

import android.media.MediaMuxer
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import com.shepeliev.webrtckmp.ApplicationContextHolder
import com.shepeliev.webrtckmp.MediaStream
import com.shepeliev.webrtckmp.audioTracks
import com.shepeliev.webrtckmp.videoTracks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.webrtc.Logging
import java.io.File
import java.util.UUID

actual class MediaRecorder actual constructor(
    private val stream: MediaStream,
    private val options: MediaRecorderOptions,
) {

    private val _state = MutableStateFlow<MediaRecorderState>(MediaRecorderState.Inactive)
    actual val state: StateFlow<MediaRecorderState> = _state

    private val _onDataAvailable = MutableSharedFlow<String>(extraBufferCapacity = Channel.UNLIMITED)
    actual val onDataAvailable: Flow<String> = _onDataAvailable

    private val hasVideoTrack = stream.videoTracks.isNotEmpty()
    private val hasAudioTrack = stream.audioTracks.any { it.audioSource != null }
    private val numberOfTracks = getNumberOfTracks()
    private val tracksMetadata = mutableMapOf<String, TrackMetadata>()

    private var outputFilePath: String? = null
    private var recorderThread: HandlerThread? = null
    private var videoEncoder: VideoEncoder? = null
    private var audioEncoder: AudioEncoder? = null
    private var muxer: MediaMuxer? = null
    private var muxerStarted = false

    private fun getNumberOfTracks(): Int {
        val numOfVideoTracks = stream.videoTracks.size.coerceAtMost(1)
        val numOfAudioTracks = stream.audioTracks.filter { it.audioSource != null }.size.coerceAtMost(1)
        return numOfVideoTracks + numOfAudioTracks
    }

    actual suspend fun start() {
        Logging.v(TAG, "Start")
        check(state.value == MediaRecorderState.Inactive) {
            "Recording can be started in Inactive state only. Current state is ${state.value}"
        }
        _state.value = MediaRecorderState.Recording
        tracksMetadata.clear()

        runCatching {
            initMuxer()
            val thread = createRecorderThread()
            val handler = Handler(thread.looper)
            videoEncoder = hasVideoTrack.takeIf { it }?.let {
                Logging.v(TAG, "Create video encoder")
                VideoEncoder(
                    videoTrack = stream.videoTracks.firstOrNull(),
                    frameRate = options.videoFrameRate,
                    bitsPerSecond = options.videoBitsPerSeconds,
                    handler = handler,
                    onData = { encodedData, error -> processEncodedData("video", encodedData, error) }
                )
            }
            audioEncoder = hasAudioTrack.takeIf { it }?.let {
                Logging.v(TAG, "Create audio encoder")
                AudioEncoder(
                    options.audioBitsPerSeconds,
                    handler,
                    onData = { encodedData, error -> processEncodedData("audio", encodedData, error) }
                )
            }
        }.onFailure { stopExceptionally(it) }
    }

    actual fun stop() {
        Logging.v(TAG, "Stop")
        if (state.value != MediaRecorderState.Recording) return
        _state.value = MediaRecorderState.Stopping
        videoEncoder?.stop()
        audioEncoder?.stop()
    }

    private suspend fun initMuxer() {
        outputFilePath = getOutputPath(options.mediaFormat.mimeType)
            .also { muxer = MediaMuxer(it, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4) }
        muxerStarted = false
        Logging.v(TAG, "Media output file path: $outputFilePath")
    }

    private suspend fun getOutputPath(mimeType: MimeType): String = withContext(Dispatchers.IO) {
        val context = ApplicationContextHolder.context
        val isExternalStorageAvailable = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
        val cacheDir: File = if (isExternalStorageAvailable) {
            context.externalCacheDir ?: context.cacheDir
        } else {
            context.cacheDir
        }

        File.createTempFile("media", ".${mimeType.fileExtension}", cacheDir).absolutePath
    }

    private fun createRecorderThread(): HandlerThread {
        return HandlerThread("MediaRecorder-${UUID.randomUUID()}").also {
            recorderThread = it
            it.start()
        }
    }

    private fun processEncodedData(track: String, encodedData: EncodedData?, error: Throwable?) {
        val muxer = checkNotNull(muxer)

        if (error != null || encodedData == null) {
            error?.let(::stopExceptionally)
            return
        }

        val (trackId, finished) = tracksMetadata.getOrPut(track) {
            Logging.v(TAG, """
                |Adding $track track to muxer.
                |   Media format: ${encodedData.mediaFormat}.
                |   Number of added tracks: ${tracksMetadata.size + 1}. 
                |   Total tracks: $numberOfTracks
                |""".trimMargin())
            val trackId = muxer.addTrack(encodedData.mediaFormat)
            TrackMetadata(trackId).also {
                if (tracksMetadata.size == numberOfTracks - 1) {
                    Logging.v(TAG, "Start muxer")
                    muxer.start()
                    muxerStarted = true
                }
            }
        }

        if (finished) return

        if (encodedData.bufferInfo.isEndOfStream) {
            Logging.v(TAG, "$track track finished")
            tracksMetadata[track] = TrackMetadata(trackId, finished = true)
        } else if (muxerStarted) {
            muxer.writeSampleData(trackId, encodedData.buffer, encodedData.bufferInfo)
        }

        val allTracksFinished = tracksMetadata.values.all { it.finished }
        if (allTracksFinished) completeRecording()
    }

    private fun stopExceptionally(error: Throwable) {
        Logging.w(TAG, "Stop exceptionally", error)
        tracksMetadata.mapValues { (_, v) -> v.copy(finished = true) }
        disposeRecorder()
        outputFilePath?.let {
            runCatching { File(it).delete() }
                .onFailure { Logging.e(TAG, "Deleting temporary file failed", it) }
        }
        _state.value = MediaRecorderState.Failed(error)
    }

    private fun completeRecording() {
        Logging.v(TAG, "Complete recording. Output file path: $outputFilePath")
        _state.value = MediaRecorderState.Inactive
        disposeRecorder()
        _onDataAvailable.tryEmit(outputFilePath!!)
    }

    private fun disposeRecorder() {
        Logging.v(TAG, "Dispose recorder")
        videoEncoder?.dispose()
        videoEncoder = null
        audioEncoder?.dispose()
        audioEncoder = null

        muxer?.stop()
        muxer?.release()
        muxer = null

        recorderThread?.quit()
        recorderThread = null
    }
}

private const val TAG = "MediaRecorder"

private data class TrackMetadata(val id: Int = -1, val finished: Boolean = false)
