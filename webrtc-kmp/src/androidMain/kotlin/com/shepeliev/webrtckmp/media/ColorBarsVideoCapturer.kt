package com.shepeliev.webrtckmp.media

import android.content.Context
import android.view.Surface
import android.view.WindowManager
import androidx.core.content.getSystemService
import com.shepeliev.webrtckmp.DEFAULT_FRAME_RATE
import com.shepeliev.webrtckmp.DEFAULT_VIDEO_HEIGHT
import com.shepeliev.webrtckmp.DEFAULT_VIDEO_WIDTH
import com.shepeliev.webrtckmp.VideoTrackConstraints
import com.shepeliev.webrtckmp.yuv.YuvColorBars
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.webrtc.JavaI420Buffer
import org.webrtc.Size
import org.webrtc.VideoFrame
import org.webrtc.VideoSource
import java.nio.ByteBuffer
import kotlin.time.Duration.Companion.milliseconds

internal class ColorBarsVideoCapturer(
    context: Context,
    private val videoSource: VideoSource,
    override val constraints: VideoTrackConstraints,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : VideoCapturer {
    override val isScreencast: Boolean = false

    private val windowManager = context.getSystemService<WindowManager>()
    private val currentOrientation get() = if (getRotation() % 180 == 0) Orientation.Portrait else Orientation.Landscape
    private var prevOrientation = currentOrientation

    private val videoSize
        get() = when (currentOrientation) {
            Orientation.Landscape -> Size(
                constraints.width?.value ?: DEFAULT_VIDEO_WIDTH,
                constraints.height?.value ?: DEFAULT_VIDEO_HEIGHT
            )
            Orientation.Portrait -> Size(
                constraints.height?.value ?: DEFAULT_VIDEO_HEIGHT,
                constraints.width?.value ?: DEFAULT_VIDEO_WIDTH
            )
        }

    private val fps = constraints.frameRate?.value?.toInt() ?: DEFAULT_FRAME_RATE
    private val frameInterval = (1000 / fps).milliseconds

    private var colorBars = videoSize.let { YuvColorBars(it.width, it.height) }
    private val dataY = ByteBuffer.allocateDirect(colorBars.yStrides.size).apply { put(colorBars.yStrides) }
    private val dataU = ByteBuffer.allocateDirect(colorBars.uStrides.size).apply { put(colorBars.uStrides) }
    private val dataV = ByteBuffer.allocateDirect(colorBars.vStrides.size).apply { put(colorBars.vStrides) }
    private val scope = CoroutineScope(dispatcher)
    private var captureJob: Job? = null

    override fun addErrorListener(errorListener: VideoCapturerErrorListener) {
        // this capturer should not emmit any error
    }

    override fun startCapture() {
        checkIsNotDisposed()
        if (captureJob?.isActive == true) return
        @Suppress("InconsistentCommentForJavaParameter")
        videoSource.capturerObserver.onCapturerStarted(/* success = */ true)
        captureJob = scope.launch {
            while (isActive) {
                val size = videoSize

                if (prevOrientation != currentOrientation) {
                    prevOrientation = currentOrientation
                    colorBars = size.let { YuvColorBars(it.width, it.height) }
                    dataY.put(colorBars.yStrides)
                    dataU.put(colorBars.uStrides)
                    dataV.put(colorBars.vStrides)
                }

                dataY.rewind()
                dataU.rewind()
                dataV.rewind()

                val frame = JavaI420Buffer.wrap(
                    size.width,
                    size.height,
                    dataY,
                    size.width,
                    dataU,
                    colorBars.uvStrideSize,
                    dataV,
                    colorBars.uvStrideSize,
                    null
                ).let { VideoFrame(it, 0, System.nanoTime()) }
                videoSource.capturerObserver.onFrameCaptured(frame)

                delay(frameInterval)
            }
        }
    }

    private fun getRotation(): Int {
        @Suppress("DEPRECATION")
        return when (windowManager?.defaultDisplay?.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }
    }

    override fun stopCapture() {
        captureJob?.cancel()
        videoSource.capturerObserver.onCapturerStopped()
    }

    private fun checkIsNotDisposed() {
        check(scope.isActive) { "ColorBarsVideoCapturer has been disposed" }
    }

    override fun dispose() {
        stopCapture()
        scope.cancel()
    }
}

private enum class Orientation { Landscape, Portrait }
