package com.shepeliev.webrtckmp.mediarecorder

import android.os.Handler
import android.os.HandlerThread
import com.shepeliev.webrtckmp.FLOW_BUFFER_CAPACITY
import com.shepeliev.webrtckmp.MediaStream
import com.shepeliev.webrtckmp.videoTracks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.onEach
import org.webrtc.Logging
import org.webrtc.VideoSink
import java.util.UUID

actual class MediaRecorder actual constructor(
    private val stream: MediaStream,
    private val options: MediaRecorderOptions
) {

    private val tag = "MediaRecorder"

    private val _onDataAvailable = MutableSharedFlow<String>(extraBufferCapacity = FLOW_BUFFER_CAPACITY)
    actual val onDataAvailable: Flow<String> = _onDataAvailable
        .asSharedFlow()
        .onEach { Logging.d(tag, "onDataAvailable: $it") }

    private val _onError = MutableSharedFlow<Throwable>(extraBufferCapacity = FLOW_BUFFER_CAPACITY)
    actual val onError: Flow<Throwable> = _onError.asSharedFlow()
        .onEach { Logging.e(tag, "onError: $it") }

    private val _onPause = MutableSharedFlow<Unit>(extraBufferCapacity = FLOW_BUFFER_CAPACITY)
    actual val onPause: Flow<Unit> = _onPause.asSharedFlow()
        .onEach { Logging.d(tag, "onPause [state = $state]") }

    private val _onResume = MutableSharedFlow<Unit>(extraBufferCapacity = FLOW_BUFFER_CAPACITY)
    actual val onResume: Flow<Unit> = _onResume.asSharedFlow()
        .onEach { Logging.d(tag, "onResume [state = $state]") }

    private val _onStart = MutableSharedFlow<Unit>(extraBufferCapacity = FLOW_BUFFER_CAPACITY)
    actual val onStart: Flow<Unit> = _onStart.asSharedFlow()
        .onEach { Logging.d(tag, "onStart [state = $state]") }

    private val _onStop = MutableSharedFlow<Unit>(extraBufferCapacity = FLOW_BUFFER_CAPACITY)
    actual val onStop: Flow<Unit> = _onStop.asSharedFlow()
        .onEach { Logging.d(tag, "onStop [state = $state]") }

    private var _state = MediaRecorderState.Inactive
    actual val state: MediaRecorderState get() = _state

    private var muxerController: MediaMuxerController? = null
    private var recorderThread: HandlerThread? = null
    private var recorderHandler: Handler? = null
    private var videoEncoder: VideoEncoder? = null
    private var encodersStarted = true

    private val videoSink = VideoSink { frame ->
        videoEncoder?.let { encoder ->
            frame.retain()
            recorderHandler?.post { encoder.encodeVideoFrame(frame) }
        }
    }

    actual fun pause() {
        check(state != MediaRecorderState.Inactive) { "MediaRecorder can't be paused in $state state." }
        if (state == MediaRecorderState.Paused) return
        _state = MediaRecorderState.Paused
        removeSinks()
        _onPause.tryEmit(Unit)
    }

    actual fun requestData() {
        videoEncoder?.stop()
    }

    actual fun resume() {
        check(state != MediaRecorderState.Inactive) { "MediaRecorder can't be resumed in $state state." }
        if (state == MediaRecorderState.Recording) return
        _state = MediaRecorderState.Recording
        addSinks()
        _onResume.tryEmit(Unit)
    }

    actual fun start(timeSliceMillis: Long) {
        check(state == MediaRecorderState.Inactive) { "MediaRecorder must be in $state state in order to start." }
        _state = MediaRecorderState.Recording

        recorderThread = HandlerThread("MediaRecorder-${UUID.randomUUID()}").also { thread ->
            thread.start()
            recorderHandler = Handler(thread.looper)
        }

        recorderHandler?.post { startRecording(timeSliceMillis) }
    }

    private fun startRecording(timeSliceMillis: Long) {
        Logging.d(tag, "Start recording [timeSliceMillis = $timeSliceMillis]")
        val muxer = MediaMuxerController(
            mimeType = options.mediaFormat.mimeType,
            onDataAvailable = { handleDataFromMuxer(it, timeSliceMillis) }
        ).also { muxerController = it }

        runCatching {
            videoEncoder = VideoEncoder(
                bitsPerSecond = options.videoBitsPerSeconds,
                onMediaFormatReady = muxer::addVideoTrack,
                onVideoDataAvailable = { data, info -> muxer.writeVideoSampleData(data, info) },
                onStreamEnded = muxer::onVideoTrackFinished,
                onError = ::handleErrorFromEncoder
            )
            encodersStarted = true
        }.onFailure(::disposeAndThrow)

        addSinks()

        if (timeSliceMillis > -1) {
            recorderHandler?.postDelayed(::stopEncoders, timeSliceMillis)
        }

        _onStart.tryEmit(Unit)
    }

    actual fun stop() {
        check(state != MediaRecorderState.Inactive) { "Media recorder can't stop in $state state." }
        _state = MediaRecorderState.Inactive
        recorderHandler?.post { stopEncoders() }
    }

    private fun addSinks() {
        stream.videoTracks.firstOrNull()?.addSink(videoSink)
            ?: disposeAndThrow(IllegalStateException("MediaStream video tracks has been changed."))
    }

    private fun removeSinks() {
        stream.videoTracks.firstOrNull()?.removeSink(videoSink)
            ?: disposeAndThrow(IllegalStateException("MediaStream video tracks has been changed."))
    }

    private fun stopEncoders() {
        if (!encodersStarted) return
        encodersStarted = false
        Logging.d(tag, "Stop encoders")
        removeSinks()
        videoEncoder?.stop()
    }

    private fun handleDataFromMuxer(filePath: String, timeSliceMillis: Long) {
        Logging.d(tag, "handleDataFromMuxer [state: $state]")
        _onDataAvailable.tryEmit(filePath)
        disposeEncoders()

        if (state == MediaRecorderState.Inactive) {
            disposeThread()
            _onStop.tryEmit(Unit)
        } else {
            recorderHandler?.post { startRecording(timeSliceMillis) }
        }
    }

    private fun handleErrorFromEncoder(error: Throwable) {
        _onError.tryEmit(error)
        stop()
    }

    private fun disposeEncoders() {
        Logging.d(tag, "Dispose encoders")
        muxerController = null
        videoEncoder?.dispose()
        videoEncoder = null
    }

    private fun disposeThread() {
        Logging.d(tag, "Dispose recorder thread")
        recorderThread?.quit()
        recorderThread = null
        recorderHandler = null
    }

    private fun disposeAndThrow(error: Throwable): Nothing {
        disposeEncoders()
        disposeThread()
        _state = MediaRecorderState.Inactive
        throw error
    }
}
