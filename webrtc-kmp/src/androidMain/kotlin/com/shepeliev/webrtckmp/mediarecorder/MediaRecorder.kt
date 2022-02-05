package com.shepeliev.webrtckmp.mediarecorder

import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
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
import java.io.IOException
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
                    Log.w("DEBUG", "Prepare: ${frame.rotatedWidth} x ${frame.rotatedHeight}")
                    prepareVideoRecorder(frame.rotatedWidth, frame.rotatedHeight, frame.rotation)
                }
                Log.w("DEBUG", "Render: ${frame.rotatedWidth} x ${frame.rotatedHeight}")
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

    private fun prepareVideoRecorder(outputWidth: Int, outputHeight: Int, rotation: Int) {
        androidMediaRecorder = AndroidMediaRecorder().apply {

            // Step 2: Set sources
            setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
            setVideoSource(MediaRecorder.VideoSource.SURFACE)

            // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
            setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW))
            setVideoSize(outputWidth, outputHeight)

            // Step 4: Set output file
            outputPath = getOutputPath(options.mediaFormat.mimeType)
            setOutputFile(outputPath!!)

            setOnErrorListener { mr, what, extra ->
                _onError.tryEmit(RuntimeException("Media record error: [what = $what, extra = $extra]"))
                stop()
            }

            // Step 6: Prepare configured MediaRecorder
            try {
                prepare()

                frameDrawer = VideoFrameDrawer()
                recordableEglBase = EglBase.create(rootEglBase.eglBaseContext, EglBase.CONFIG_RECORDABLE).apply {
                    createSurface(surface)
                    makeCurrent()
                }

                start()
                _onStart.tryEmit(Unit)
            } catch (e: IllegalStateException) {
                Logging.e(tag, "IllegalStateException preparing MediaRecorder: ${e.message}")
                releaseMediaRecorder()
                throw e
            } catch (e: IOException) {
                Logging.e(tag, "IOException preparing MediaRecorder: ${e.message}")
                releaseMediaRecorder()
                throw e
            }
        }
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
        androidMediaRecorder?.reset() // clear recorder configuration
        androidMediaRecorder?.release() // release the recorder object
        androidMediaRecorder = null
        outputPath = null
        _state = MediaRecorderState.Inactive
    }

//    private fun startRecording() {
//        val muxer = MediaMuxerController(
//            mimeType = options.mediaFormat.mimeType,
//            onDataAvailable = ::handleDataFromMuxer
//        ).also { muxerController = it }
//
//        runCatching {
//            videoEncoder = VideoEncoder(
//                bitsPerSecond = options.videoBitsPerSeconds,
//                onMediaFormatReady = muxer::addVideoTrack,
//                onVideoDataAvailable = { data, info -> muxer.writeVideoSampleData(data, info) },
//                onStreamEnded = muxer::onVideoTrackFinished,
//                onError = ::handleErrorFromEncoder
//            )
//        }.onFailure(::disposeAndThrow)
//
//        addSinks()
//
//        _onStart.tryEmit(Unit)
//    }

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

//        recorderHandler?.post { stopEncoders() }
    }

    private fun addSinks() {
        stream.videoTracks.firstOrNull()?.addSink(videoSink)
            ?: disposeAndThrow(IllegalStateException("MediaStream video tracks has been changed."))
    }

    private fun removeSinks() {
        stream.videoTracks.firstOrNull()?.removeSink(videoSink)
            ?: disposeAndThrow(IllegalStateException("MediaStream video tracks has been changed."))
    }

//    private fun stopEncoders() {
//        Logging.d(tag, "Stop encoders")
//        removeSinks()
//        videoEncoder?.stop()
//    }

//    private fun handleDataFromMuxer(filePath: String) {
//        Logging.d(tag, "handleDataFromMuxer [state: $state]")
//        disposeEncoders()
//        disposeThread()
//        _onDataAvailable.tryEmit(filePath)
//        _onStop.tryEmit(Unit)
//    }

    private fun handleErrorFromEncoder(error: Throwable) {
        _onError.tryEmit(error)
        stop()
    }

//    private fun disposeEncoders() {
//        Logging.d(tag, "Dispose encoders")
//        muxerController = null
//        videoEncoder?.dispose()
//        videoEncoder = null
//    }

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
