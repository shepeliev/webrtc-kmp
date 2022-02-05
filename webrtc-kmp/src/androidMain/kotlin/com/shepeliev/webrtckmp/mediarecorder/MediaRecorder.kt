package com.shepeliev.webrtckmp.mediarecorder

import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import com.shepeliev.webrtckmp.FLOW_BUFFER_CAPACITY
import com.shepeliev.webrtckmp.MediaStream
import com.shepeliev.webrtckmp.applicationContext
import com.shepeliev.webrtckmp.rootEglBase
import com.shepeliev.webrtckmp.videoTracks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.onEach
import org.webrtc.EglBase
import org.webrtc.GlRectDrawer
import org.webrtc.Logging
import org.webrtc.VideoFrameDrawer
import org.webrtc.VideoSink
import java.io.File
import java.util.UUID
import android.media.MediaRecorder as AndroidMediaRecorder

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

    private var recorderThread: HandlerThread? = null
    private var recorderHandler: Handler? = null
    private val drawer = GlRectDrawer()
    private var frameDrawer: VideoFrameDrawer? = null
    private var recordableEglBase: EglBase? = null
    private var androidMediaRecorder: AndroidMediaRecorder? = null
    private var outputPath: String? = null

    private val videoSink = VideoSink { frame ->
        frame.retain()
        recorderHandler?.post {
            runCatching {
                if (androidMediaRecorder == null) {
                    prepareVideoRecorder(frame.rotatedWidth, frame.rotatedHeight)
                }
                frameDrawer?.drawFrame(frame, drawer, null, 0, 0, frame.rotatedWidth, frame.rotatedHeight)
                recordableEglBase?.swapBuffers()
            }
            frame.release()
        }
    }

    actual fun pause() {
        Logging.e(tag, "MediaRecorder.pause() is not supported on Android yet.")
    }

    actual fun requestData() {
        Logging.e(tag, "MediaRecorder.requestData() is not supported on Android yet.")
    }

    actual fun resume() {
        Logging.e(tag, "MediaRecorder.pause() is not supported on Android.")
    }

    actual fun start(timeSliceMillis: Long) {
        check(state == MediaRecorderState.Inactive) { "MediaRecorder must be in $state state in order to start." }
        if (timeSliceMillis > -1) {
            Logging.w(tag, "timeSlice param is not supported on Android yet.")
        }
        _state = MediaRecorderState.Recording

        recorderThread = HandlerThread("MediaRecorder-${UUID.randomUUID()}").also { thread ->
            thread.start()
            recorderHandler = Handler(thread.looper)
        }

        addSinks()
    }

    private fun prepareVideoRecorder(outputWidth: Int, outputHeight: Int) {
        androidMediaRecorder = AndroidMediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setProfile(CamcorderProfile.get(options.quality.toCamcorderProfile()))
            setVideoSize(outputWidth, outputHeight)
            outputPath = getOutputPath(options.mediaFormat.mimeType)
            setOutputFile(outputPath!!)

            setOnErrorListener { _, what, extra ->
                _onError.tryEmit(RuntimeException("Media record error: [what = $what, extra = $extra]"))
                stop()
            }

            try {
                prepare()
                frameDrawer = VideoFrameDrawer()
                recordableEglBase = EglBase.create(rootEglBase.eglBaseContext, EglBase.CONFIG_RECORDABLE).apply {
                    createSurface(surface)
                    makeCurrent()
                }
                start()
                _onStart.tryEmit(Unit)
            } catch (e: Throwable) {
                releaseMediaRecorder()
                throw e
            }
        }
    }

    private fun MediaRecorderQuality.toCamcorderProfile() = when (this) {
        MediaRecorderQuality.Low -> CamcorderProfile.QUALITY_LOW
        MediaRecorderQuality.Quality480P -> CamcorderProfile.QUALITY_480P
        MediaRecorderQuality.Quality720P -> CamcorderProfile.QUALITY_720P
        MediaRecorderQuality.Quality1080P -> CamcorderProfile.QUALITY_HIGH_SPEED_1080P
        MediaRecorderQuality.High -> CamcorderProfile.QUALITY_HIGH
    }

    private fun getOutputPath(mimeType: MimeType): String {
        val isExternalStorageAvailable = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
        val cacheDir: File = if (isExternalStorageAvailable) {
            applicationContext.externalCacheDir ?: applicationContext.cacheDir
        } else {
            applicationContext.cacheDir
        }

        return File.createTempFile("media", ".${mimeType.fileExtension}", cacheDir).absolutePath
    }

    private fun releaseMediaRecorder() {
        removeSinks()
        frameDrawer?.release()
        frameDrawer = null
        recordableEglBase?.releaseSurface()
        recordableEglBase?.release()
        recordableEglBase = null
        androidMediaRecorder?.reset()
        androidMediaRecorder?.release()
        androidMediaRecorder = null
        outputPath = null
        _state = MediaRecorderState.Inactive
    }

    actual fun stop() {
        check(state != MediaRecorderState.Inactive) { "Media recorder can't stop in $state state." }
        _state = MediaRecorderState.Inactive
        removeSinks()
        runCatching { androidMediaRecorder?.stop() }
            .onSuccess { _onDataAvailable.tryEmit(outputPath!!) }
            .onFailure { File(outputPath!!).delete() }
        releaseMediaRecorder()
        disposeThread()
        _onStop.tryEmit(Unit)
    }

    private fun addSinks() {
        stream.videoTracks.firstOrNull()?.addSink(videoSink)
            ?: disposeAndThrow(IllegalStateException("MediaStream video tracks has been changed."))
    }

    private fun removeSinks() {
        stream.videoTracks.firstOrNull()?.removeSink(videoSink)
            ?: disposeAndThrow(IllegalStateException("MediaStream video tracks has been changed."))
    }

    private fun disposeThread() {
        Logging.d(tag, "Dispose recorder thread")
        recorderThread?.quit()
        recorderThread = null
        recorderHandler = null
    }

    private fun disposeAndThrow(error: Throwable): Nothing {
        releaseMediaRecorder()
        disposeThread()
        _state = MediaRecorderState.Inactive
        throw error
    }
}
