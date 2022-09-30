package com.shepeliev.webrtckmp.media

import android.content.Context
import com.shepeliev.webrtckmp.CameraVideoCapturerException
import com.shepeliev.webrtckmp.DEFAULT_FRAME_RATE
import com.shepeliev.webrtckmp.DEFAULT_VIDEO_HEIGHT
import com.shepeliev.webrtckmp.DEFAULT_VIDEO_WIDTH
import com.shepeliev.webrtckmp.FacingMode
import com.shepeliev.webrtckmp.VideoTrackConstraints
import com.shepeliev.webrtckmp.WebRtc
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraEnumerationAndroid
import org.webrtc.CameraEnumerator
import org.webrtc.Size
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoSource
import java.util.UUID
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import org.webrtc.CameraVideoCapturer as WebRtcCameraVideoCapturer

internal class CameraVideoCapturer(
    private val context: Context,
    private val videoSource: VideoSource,
    override val constraints: VideoTrackConstraints,
) : VideoCapturer {

    override val isScreencast: Boolean get() = cameraVideoCapturer.isScreencast

    private val textureHelper: SurfaceTextureHelper = SurfaceTextureHelper.create(
        "VideoCapturerTextureHelper-${UUID.randomUUID()}",
        WebRtc.rootEglBase.eglBaseContext,
    )
    private val cameraVideoCapturer: WebRtcCameraVideoCapturer
    private val videoSize: Size
    private val videoFps: Int
    private var disposed = false
    private val errorListeners = mutableSetOf<VideoCapturerErrorListener>()

    init {
        val enumerator = Camera2Enumerator(context)
        val deviceId = selectDeviceId(enumerator)
        videoSize = selectVideoSize(enumerator, deviceId)
        videoFps = selectFps(enumerator, deviceId)
        cameraVideoCapturer = enumerator.createCapturer(deviceId, CameraEventsHandler())
            .apply { initialize(textureHelper, context, videoSource.capturerObserver) }

    }

    private fun selectDeviceId(enumerator: CameraEnumerator): String {
        val deviceId = constraints.deviceId
        val isFrontFacing = constraints.facingMode?.exact == FacingMode.User ||
            constraints.facingMode?.ideal == FacingMode.User

        val searchCriteria: (String) -> Boolean = if (deviceId != null) {
            { it == deviceId }
        } else {
            { enumerator.isFrontFacing(it) == isFrontFacing }
        }

        return enumerator.deviceNames.firstOrNull(searchCriteria)
            ?: throw CameraVideoCapturerException.notFound(constraints)
    }

    private fun selectVideoSize(enumerator: CameraEnumerator, deviceId: String): Size {
        val requestedWidth = constraints.width?.exact
            ?: constraints.width?.ideal
            ?: DEFAULT_VIDEO_WIDTH
        val requestedHeight = constraints.height?.exact
            ?: constraints.height?.ideal
            ?: DEFAULT_VIDEO_HEIGHT

        val formats = enumerator.getSupportedFormats(deviceId)
        val sizes = formats?.map { Size(it.width, it.height) } ?: emptyList()
        if (sizes.isEmpty()) throw CameraVideoCapturerException.notFound(constraints)

        return CameraEnumerationAndroid.getClosestSupportedSize(
            sizes,
            requestedWidth,
            requestedHeight
        )
    }

    private fun selectFps(enumerator: CameraEnumerator, deviceId: String): Int {
        val requestedFps = constraints.frameRate?.exact
            ?: constraints.frameRate?.ideal
            ?: DEFAULT_FRAME_RATE

        val formats = enumerator.getSupportedFormats(deviceId)
        val framerates = formats?.map { it.framerate } ?: emptyList()
        if (framerates.isEmpty()) throw CameraVideoCapturerException.notFound(constraints)

        val requestedFpsInt = requestedFps.toInt()
        val range = CameraEnumerationAndroid.getClosestSupportedFramerateRange(
            framerates,
            requestedFpsInt
        )

        return requestedFpsInt.coerceIn(range.min / 1000, range.max / 1000)
    }

    override fun startCapture() {
        checkIsNotDisposed()
        cameraVideoCapturer.startCapture(
            checkNotNull(videoSize.width),
            checkNotNull(videoSize.height),
            videoFps
        )
    }

    override fun stopCapture() {
        checkIsNotDisposed()
        cameraVideoCapturer.stopCapture()
    }

    override fun dispose() {
        if (disposed) return
        disposed = true
        cameraVideoCapturer.dispose()
        textureHelper.dispose()
        videoSource.dispose()
    }

    override fun addErrorListener(errorListener: VideoCapturerErrorListener) {
        errorListeners += errorListener
    }

    suspend fun switchCamera() {
        checkIsNotDisposed()
        suspendCoroutine { cameraVideoCapturer.switchCamera(switchCameraHandler(it)) }
    }

    suspend fun switchCamera(deviceId: String) {
        checkIsNotDisposed()
        suspendCoroutine { cameraVideoCapturer.switchCamera(switchCameraHandler(it), deviceId) }
    }

    private fun switchCameraHandler(continuation: Continuation<Unit>): org.webrtc.CameraVideoCapturer.CameraSwitchHandler {
        return object : org.webrtc.CameraVideoCapturer.CameraSwitchHandler {
            override fun onCameraSwitchDone(isFrontFacing: Boolean) {
                continuation.resume(Unit)
            }

            override fun onCameraSwitchError(error: String?) {
                val message = "Switch camera failed: $error"
                continuation.resumeWithException(CameraVideoCapturerException(message))
            }
        }
    }

    private fun checkIsNotDisposed() {
        check(!disposed) { "CameraVideoCapturer has been disposed" }
    }

    private inner class CameraEventsHandler : org.webrtc.CameraVideoCapturer.CameraEventsHandler {
        override fun onCameraError(errorDescription: String) {
            errorListeners.forEach { it.onError("Error: $errorDescription") }
        }

        override fun onCameraDisconnected() {
            errorListeners.forEach { it.onError("Camera disconnected") }
        }

        override fun onCameraFreezed(errorDescription: String) {
            errorListeners.forEach { it.onError("Camera freezed: $errorDescription") }
        }

        override fun onCameraOpening(cameraId: String) {
            // ignore this event
        }

        override fun onFirstFrameAvailable() {
            // ignore this event
        }

        override fun onCameraClosed() {
            // ignore this event
        }
    }
}
