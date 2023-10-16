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
    videoSource: VideoSource,
    constraints: MediaTrackConstraints,
) : VideoCaptureController(videoSource, CameraCapturerHelper.buildMediaTrackSettings(constraints)) {
    private var pendingDeviceId: String? = null

    override fun createVideoCapturer(): VideoCapturer {
        return WebRtc.cameraEnumerator.createCapturer(checkNotNull(settings.deviceId), CameraEventsHandler())
    }

    suspend fun switchCamera() {
        val deviceNames = WebRtc.cameraEnumerator.deviceNames
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
            videoCapturerStopListener.onStop("Error: $errorDescription")
        }

        override fun onCameraDisconnected() {
            videoCapturerStopListener.onStop("Camera disconnected")
        }

        override fun onCameraFreezed(errorDescription: String) {
            videoCapturerStopListener.onStop("Camera freezed: $errorDescription")
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

private object CameraCapturerHelper {
    fun buildMediaTrackSettings(constraints: MediaTrackConstraints): MediaTrackSettings {
        val deviceId = selectDevice(constraints)
        val facingMode = getFacingMode(deviceId)
        val size = selectVideoSize(constraints, deviceId)
        val frameRate = selectFps(constraints, deviceId)

        return MediaTrackSettings(
            width = size.width,
            height = size.height,
            deviceId = deviceId,
            facingMode = facingMode,
            frameRate = frameRate.toDouble()
        )
    }

    private fun selectDevice(constraints: MediaTrackConstraints): String {
        val isFrontFacing = constraints.facingMode?.value != FacingMode.Environment

        val searchCriteria: (String) -> Boolean = if (constraints.deviceId != null) {
            { it == constraints.deviceId }
        } else {
            { WebRtc.cameraEnumerator.isFrontFacing(it) == isFrontFacing }
        }

        return WebRtc.cameraEnumerator.deviceNames.firstOrNull(searchCriteria)
            ?: throw CameraVideoCapturerException.notFound(constraints)
    }

    private fun getFacingMode(deviceId: String): FacingMode {
        return if (WebRtc.cameraEnumerator.isFrontFacing(deviceId)) FacingMode.User else FacingMode.Environment
    }

    private fun selectVideoSize(constraints: MediaTrackConstraints, deviceId: String): Size {
        val requestedWidth = constraints.width?.value ?: DEFAULT_VIDEO_WIDTH
        val requestedHeight = constraints.height?.value ?: DEFAULT_VIDEO_HEIGHT
        val formats = WebRtc.cameraEnumerator.getSupportedFormats(deviceId)
        val sizes = formats?.map { Size(it.width, it.height) } ?: emptyList()
        if (sizes.isEmpty()) throw CameraVideoCapturerException.notFound(constraints)

        return CameraEnumerationAndroid.getClosestSupportedSize(sizes, requestedWidth, requestedHeight)
    }

    private fun selectFps(constraints: MediaTrackConstraints, deviceId: String): Int {
        val requestedFps = constraints.frameRate?.value ?: DEFAULT_FRAME_RATE
        val formats = WebRtc.cameraEnumerator.getSupportedFormats(deviceId)
        val frameRates = formats?.map { it.framerate } ?: emptyList()
        if (frameRates.isEmpty()) throw CameraVideoCapturerException.notFound(constraints)

        val requestedFpsInt = requestedFps.toInt()
        val range = CameraEnumerationAndroid.getClosestSupportedFramerateRange(frameRates, requestedFpsInt)

        return requestedFpsInt.coerceIn(range.min / 1000, range.max / 1000)
    }
}
