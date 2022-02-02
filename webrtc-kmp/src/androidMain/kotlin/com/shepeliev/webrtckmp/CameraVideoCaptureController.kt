package com.shepeliev.webrtckmp

import android.util.Log
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraEnumerationAndroid
import org.webrtc.CameraVideoCapturer
import org.webrtc.Size
import org.webrtc.VideoCapturer
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CameraVideoCaptureController(private val constraints: VideoTrackConstraints) :
    AbstractVideoCaptureController() {

    private val tag = "CameraCaptureController"
    private val enumerator = Camera2Enumerator(applicationContext)
    private var device: String? = null

    override fun createVideoCapturer(): VideoCapturer {
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

        return CameraEnumerationAndroid.getClosestSupportedFramerateRange(
            framerates,
            requestedFps.toInt()
        ).max
    }

    suspend fun switchCamera() {
        val cameraCapturer = videoCapturer as CameraVideoCapturer
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
            Log.e(tag, "Camera error: $errorDescription")
        }

        override fun onCameraDisconnected() {
            Log.w(tag, "Camera disconnected")
        }

        override fun onCameraFreezed(errorDescription: String) {
            Log.e(tag, "Camera freezed: $errorDescription")
        }

        override fun onCameraOpening(cameraId: String) {
            Log.d(tag, "Opening camera $cameraId")
        }

        override fun onFirstFrameAvailable() {
            Log.d(tag, "First frame available")
        }

        override fun onCameraClosed() {
            Log.d(tag, "Camera closed")
        }
    }
}
