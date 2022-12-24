package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.media.video.VideoCapture
import dev.onvoid.webrtc.media.video.VideoDesktopSource
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal class CameraVideoCaptureController(
    private val constraints: VideoTrackConstraints,
    videoSource: VideoDesktopSource,
) : VideoCaptureController(videoSource) {

    private val tag = "CameraCaptureController"
    private val enumerator = Camera2Enumerator(ApplicationContextHolder.context)
    private var device: String? = null

    override fun createVideoCapturer(): VideoCapture {
        selectDevice()
        return enumerator.createCapturer(device, CameraEventsHandler())
    }

    private fun selectDevice() {
        val deviceId = constraints.deviceId
        val isFrontFacing = constraints.facingMode?.exact == FacingMode.User ||
            constraints.facingMode?.ideal == FacingMode.User

        val searchCriteria: (String) -> Boolean = if (deviceId != null) {
            { it == deviceId }
        } else {
            { enumerator.isFrontFacing(it) == isFrontFacing }
        }

        device = enumerator.deviceNames.firstOrNull(searchCriteria)
            ?: throw CameraVideoCapturerException.notFound(constraints)
    }

    override fun selectVideoSize(): Size {
        val requestedWidth = constraints.width?.exact
            ?: constraints.width?.ideal
            ?: DEFAULT_VIDEO_WIDTH
        val requestedHeight = constraints.height?.exact
            ?: constraints.height?.ideal
            ?: DEFAULT_VIDEO_HEIGHT

        val formats = enumerator.getSupportedFormats(device)
        val sizes = formats?.map { Size(it.width, it.height) } ?: emptyList()
        if (sizes.isEmpty()) throw CameraVideoCapturerException.notFound(constraints)

        return CameraEnumerationAndroid.getClosestSupportedSize(
            sizes,
            requestedWidth,
            requestedHeight
        )
    }

    override fun selectFps(): Int {
        val requestedFps = constraints.frameRate?.exact
            ?: constraints.frameRate?.ideal
            ?: DEFAULT_FRAME_RATE

        val formats = enumerator.getSupportedFormats(device)
        val framerates = formats?.map { it.framerate } ?: emptyList()
        if (framerates.isEmpty()) throw CameraVideoCapturerException.notFound(constraints)

        val requestedFpsInt = requestedFps.toInt()
        val range = CameraEnumerationAndroid.getClosestSupportedFramerateRange(
            framerates,
            requestedFpsInt
        )

        return requestedFpsInt.coerceIn(range.min / 1000, range.max / 1000)
    }

    suspend fun switchCamera() {
        val cameraCapturer = videoCapturer as CameraVideoCaptureController
        suspendCoroutine<Unit> { cameraCapturer.switchCamera(switchCameraHandler(it)) }
    }

    suspend fun switchCamera(deviceId: String) {
        val cameraCapturer = videoCapturer as CameraVideoCapturer
        suspendCoroutine<Unit> { cameraCapturer.switchCamera(switchCameraHandler(it), deviceId) }
    }

    private fun switchCameraHandler(continuation: Continuation<Unit>): CameraVideoCapturer.CameraSwitchHandler {
        return object : CameraVideoCapturer.CameraSwitchHandler {
            override fun onCameraSwitchDone(isFrontFacing: Boolean) {
                Log.d(tag, "Camera switched. isFront: $isFrontFacing")
                continuation.resume(Unit)
            }

            override fun onCameraSwitchError(error: String?) {
                val message = "Switch camera failed: $error"
                Log.e(tag, message)
                continuation.resumeWithException(CameraVideoCapturerException(message))
            }
        }
    }

    private inner class CameraEventsHandler : CameraVideoCapturer.CameraEventsHandler {
        override fun onCameraError(errorDescription: String) {
            videoCapturerErrorListener.onError("Error: $errorDescription")
        }

        override fun onCameraDisconnected() {
            videoCapturerErrorListener.onError("Camera disconnected")
        }

        override fun onCameraFreezed(errorDescription: String) {
            videoCapturerErrorListener.onError("Camera freezed: $errorDescription")
        }

        override fun onCameraOpening(cameraId: String) {
            // Do nothing
        }

        override fun onFirstFrameAvailable() {
            // Do nothing
        }

        override fun onCameraClosed() {
            // Do nothing
        }
    }
}
