package com.shepeliev.webrtckmp

import org.webrtc.CameraEnumerationAndroid
import org.webrtc.CameraVideoCapturer
import org.webrtc.Size
import org.webrtc.VideoCapturer
import org.webrtc.VideoSource
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal class CameraVideoCaptureController(
    private val constraints: MediaTrackConstraints,
    videoSource: VideoSource,
) : VideoCaptureController(videoSource) {

    private val enumerator = WebRtc.cameraEnumerator
    private var pendingDeviceId: String? = null

    override fun createVideoCapturer(): VideoCapturer {
        selectDevice()
        return enumerator.createCapturer(
            checkNotNull(settings.deviceId),
            CameraEventsHandler()
        )
    }

    private fun selectDevice() {
        val isFrontFacing = constraints.facingMode?.value != FacingMode.Environment

        val searchCriteria: (String) -> Boolean = if (constraints.deviceId != null) {
            { it == constraints.deviceId }
        } else {
            { enumerator.isFrontFacing(it) == isFrontFacing }
        }

        val deviceId = enumerator.deviceNames.firstOrNull(searchCriteria)
            ?: throw CameraVideoCapturerException.notFound(constraints)

        settings = settings.copy(
            deviceId = deviceId,

            facingMode = (
                if (isFrontFacing) {
                    FacingMode.User
                } else {
                    FacingMode.Environment
                }
                )
        )
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
