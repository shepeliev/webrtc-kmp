@file:JvmName("AndroidMediaRecorder")

package com.shepeliev.webrtckmp.media

import android.media.MediaMuxer
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import com.shepeliev.webrtckmp.ApplicationContextHolder
import com.shepeliev.webrtckmp.MediaStream
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

    private val context = ApplicationContextHolder.context
    private var recorderThread: HandlerThread? = null
    private var videoEncoder: VideoEncoder? = null
    private var muxer: MediaMuxer? = null

    private var outputFilePath: String? = null
    private var videoTrackId = -1
    private var isVideoTrackFinished = false

    actual suspend fun start() {
        check(state.value == MediaRecorderState.Inactive) {
            "Recording can be started in Inactive state only. Current state is ${state.value}"
        }
        _state.value = MediaRecorderState.Recording
        videoTrackId = -1
        isVideoTrackFinished = false

        runCatching {
            initMuxer()
            val thread = createRecorderThread()
            val handler = Handler(thread.looper)
            videoEncoder = VideoEncoder(
                videoTrack = stream.videoTracks.firstOrNull(),
                frameRate = options.videoFrameRate,
                bitsPerSecond = options.videoBitsPerSeconds,
                handler = handler,
                onData = ::processVideoData
            )
        }.onFailure { stopExceptionally(it) }
    }

    actual fun stop() {
        if (state.value != MediaRecorderState.Recording) return
        _state.value = MediaRecorderState.Stopping
        videoEncoder?.stop()
    }

    private suspend fun initMuxer() {
        outputFilePath = getOutputPath(options.mediaFormat.mimeType)
            .also { muxer = MediaMuxer(it, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4) }
    }

    private suspend fun getOutputPath(mimeType: MimeType): String = withContext(Dispatchers.IO) {
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

    private fun processVideoData(encodedData: EncodedData?, error: Throwable?) {
        val muxer = checkNotNull(muxer)

        if (error != null || encodedData == null || isVideoTrackFinished) {
            error?.let(::stopExceptionally)
            return
        }

        if (videoTrackId == -1) {
            videoTrackId = muxer.addTrack(encodedData.mediaFormat)
            muxer.start()
        }

        if (encodedData.bufferInfo.isEndOfStream) {
            isVideoTrackFinished = true
        } else {
            muxer.writeSampleData(videoTrackId, encodedData.buffer, encodedData.bufferInfo)
        }

        if (isVideoTrackFinished) completeRecording()
    }

    private fun stopExceptionally(error: Throwable) {
        isVideoTrackFinished = true
        disposeRecorder()
        outputFilePath?.let {
            runCatching { File(it).delete() }
                .onFailure { Logging.e(TAG, "Deleting temporary file failed", it) }
        }
        _state.value = MediaRecorderState.Failed(error)
    }

    private fun completeRecording() {
        _state.value = MediaRecorderState.Inactive
        disposeRecorder()
        _onDataAvailable.tryEmit(outputFilePath!!)
    }

    private fun disposeRecorder() {
        videoEncoder?.dispose()
        videoEncoder = null

        muxer?.stop()
        muxer?.release()
        muxer = null

        recorderThread?.quit()
        recorderThread = null
    }
}

private const val TAG = "MediaRecorder"
