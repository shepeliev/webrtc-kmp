package com.shepeliev.webrtckmp.capturer

import com.shepeliev.webrtckmp.CameraVideoCapturerException
import com.shepeliev.webrtckmp.DEFAULT_FRAME_RATE
import com.shepeliev.webrtckmp.DEFAULT_VIDEO_HEIGHT
import com.shepeliev.webrtckmp.DEFAULT_VIDEO_WIDTH
import com.shepeliev.webrtckmp.FacingMode
import com.shepeliev.webrtckmp.MediaTrackConstraints
import com.shepeliev.webrtckmp.WebRtc
import com.shepeliev.webrtckmp.value
import org.webrtc.CameraEnumerationAndroid
import org.webrtc.CameraVideoCapturer
import org.webrtc.Size
import org.webrtc.VideoCapturer
import org.webrtc.VideoSource
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal class CameraVideoCapturerController(
    private val constraints: MediaTrackConstraints,
    videoSource: VideoSource,
) : VideoCapturerController(videoSource) {

    private val enumerator = WebRtc.cameraEnumerator
    private val cameraSelector = WebRtc.cameraSelector
    private val capturerFactory = WebRtc.cameraVideoCapturerFactory
    private var pendingDeviceId: String? = null

    override fun createVideoCapturer(): VideoCapturer {
        val deviceId = cameraSelector.selectCameraId(enumerator, constraints)
        updateSettings(deviceId)
        val capturer = capturerFactory.createCameraVideoCapturer(
            deviceId,
            enumerator,
            CameraEventsHandler()
        )
        return capturer ?: throw CameraVideoCapturerException.notFound(constraints)
    }

    private fun updateSettings(deviceId: String?) {
        val facingMode = deviceId?.let {
            if (enumerator.isFrontFacing(it)) FacingMode.User else FacingMode.Environment
        }
        settings = settings.copy(deviceId = deviceId, facingMode = facingMode)
    }

    override fun selectVideoSize(): Size {
        val requestedWidth = constraints.width?.value ?: DEFAULT_VIDEO_WIDTH
        val requestedHeight = constraints.height?.value ?: DEFAULT_VIDEO_HEIGHT
        val formats = enumerator.getSupportedFormats(checkNotNull(settings.deviceId))
        val sizes = formats?.map { Size(it.width, it.height) } ?: emptyList()
        if (sizes.isEmpty()) throw CameraVideoCapturerException.notFound(constraints)

        return CameraEnumerationAndroid.getClosestSupportedSize(
            sizes,
            requestedWidth,
            requestedHeight
        )
    }

    override fun selectFps(): Int {
        val requestedFps = constraints.frameRate?.value ?: DEFAULT_FRAME_RATE
        val formats = enumerator.getSupportedFormats(checkNotNull(settings.deviceId))
        val frameRates = formats?.map { it.framerate } ?: emptyList()
        if (frameRates.isEmpty()) throw CameraVideoCapturerException.notFound(constraints)

        val requestedFpsInt = requestedFps.toInt()
        val range = CameraEnumerationAndroid.getClosestSupportedFramerateRange(
            frameRates,
            requestedFpsInt
        )

        return requestedFpsInt.coerceIn(range.min / 1000, range.max / 1000)
    }

    suspend fun switchCamera() {
        val deviceNames = enumerator.deviceNames
        if (deviceNames.size < 2) {
            throw CameraVideoCapturerException("Can't switch camera. No other camera available.")
        }

        val deviceNameIndex = deviceNames.indexOf(checkNotNull(settings.deviceId))
        val newDeviceName = deviceNames[(deviceNameIndex + 1) % deviceNames.size]
        switchCamera(newDeviceName)
    }

    suspend fun switchCamera(deviceId: String) {
        val cameraCapturer = videoCapturer as CameraVideoCapturer
        suspendCoroutine {
            pendingDeviceId = deviceId
            cameraCapturer.switchCamera(switchCameraHandler(it), deviceId)
        }
    }

    private fun switchCameraHandler(continuation: Continuation<Unit>): CameraVideoCapturer.CameraSwitchHandler {
        return object : CameraVideoCapturer.CameraSwitchHandler {
            override fun onCameraSwitchDone(isFrontFacing: Boolean) {
                settings = settings.copy(
                    deviceId = checkNotNull(pendingDeviceId),

                    facingMode = (
                        if (isFrontFacing) {
                            FacingMode.User
                        } else {
                            FacingMode.Environment
                        }
                        )
                )

                continuation.resume(Unit)
            }

            override fun onCameraSwitchError(error: String?) {
                val message = "Switch camera failed: $error"
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
