package com.shepeliev.webrtckmp

import android.util.Log
import org.webrtc.CameraVideoCapturer
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal actual class CameraVideoCapturer actual constructor() {

    private val tag = "CameraVideoCapturer"
    private var videoCapturer: CameraVideoCapturer? = null
    private lateinit var currentCameraId: String

    actual fun startCapture(
        cameraId: String,
        constraints: VideoConstraints,
        videoSource: VideoSource
    ) {
        check(videoCapturer == null) { "Camera video capturer already started." }

        currentCameraId = cameraId

        videoCapturer = CameraEnumerator.enumerator
            .createCapturer(cameraId, CameraEventsHandler())
            .apply {
                initialize(
                    WebRtcKmp.surfaceTextureHelper,
                    WebRtcKmp.applicationContext,
                    videoSource.native.capturerObserver
                )

                val width = constraints.width ?: DEFAULT_VIDEO_WIDTH
                val height = constraints.height ?: DEFAULT_VIDEO_HEIGHT
                val fps = constraints.fps ?: DEFAULT_FRAME_RATE

                startCapture(width, height, fps)
            }
    }

    actual suspend fun switchCamera(): MediaDeviceInfo {
        checkNotNull(videoCapturer) { "Video capturing is not started" }

        val devices = CameraEnumerator.enumerateDevices()
        val currentCamera = devices.first { it.deviceId == currentCameraId }
        val nextCamera =
            devices.firstOrNull { it.isFrontFacing != currentCamera.isFrontFacing } ?: currentCamera

        switchCameraInternal(nextCamera.deviceId)
        return nextCamera
    }

    actual suspend fun switchCamera(cameraId: String): MediaDeviceInfo {
        val isFrontFacing = switchCameraInternal(cameraId)

        return MediaDeviceInfo(
            deviceId = cameraId,
            label = cameraId,
            kind = MediaDeviceKind.VideoInput,
            isFrontFacing = isFrontFacing
        )
    }

    private suspend fun switchCameraInternal(deviceId: String): Boolean {
        val capturer = videoCapturer ?: throw CameraVideoCapturerException.capturerStopped()

        return suspendCoroutine { cont ->
            capturer.switchCamera(
                object : CameraVideoCapturer.CameraSwitchHandler {
                    override fun onCameraSwitchDone(isFrontCamera: Boolean) {
                        cont.resume(isFrontCamera)
                    }

                    override fun onCameraSwitchError(errorDescription: String) {
                        cont.resumeWithException(CameraVideoCapturerException(errorDescription))
                    }
                },
                deviceId
            )
        }
    }

    actual fun stopCapture() {
        videoCapturer?.stopCapture()
        videoCapturer = null
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
