package com.shepeliev.webrtckmp

import android.content.Context
import android.util.Log
import com.shepeliev.webrtckmp.android.ApplicationContextProvider
import com.shepeliev.webrtckmp.android.EglBaseProvider
import com.shepeliev.webrtckmp.utils.uuid
import org.webrtc.Camera1Enumerator
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraEnumerationAndroid
import org.webrtc.CameraEnumerator
import org.webrtc.Size
import org.webrtc.SurfaceTextureHelper
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import org.webrtc.CameraVideoCapturer as NativeCameraVideoCapturer

internal actual object CameraEnumerator {
    private val enumerator by lazy {
        val context = ApplicationContextProvider.applicationContext
        if (Camera2Enumerator.isSupported(context)) {
            Camera2Enumerator(context)
        } else {
            Camera1Enumerator()
        }
    }

    private val applicationContext by lazy { ApplicationContextProvider.applicationContext }

    private val surfaceTextureHelper by lazy {
        val eglBase = EglBaseProvider.getEglBase()
        SurfaceTextureHelper.create(uuid(), eglBase.eglBaseContext)
    }

    actual suspend fun enumerateDevices(): List<MediaDeviceInfo> {
        return enumerator.deviceNames.map {
            MediaDeviceInfo(
                deviceId = it,
                label = it,
                kind = MediaDeviceKind.VideoInput,
                enumerator.isFrontFacing(it)
            )
        }
    }

    actual fun createCameraVideoCapturer(source: VideoSource): CameraVideoCapturer {
        return CameraVideoCapturerImpl(
            applicationContext,
            surfaceTextureHelper,
            enumerator,
            source
        )
    }
}

private class CameraVideoCapturerImpl(
    private val applicationContext: Context,
    private val surfaceTextureHelper: SurfaceTextureHelper,
    private val enumerator: CameraEnumerator,
    private val videoSource: VideoSource,
) : CameraVideoCapturer {

    private val tag = "CameraVideoCapturer"
    private lateinit var constraints: VideoConstraints
    private var videoCapturer: NativeCameraVideoCapturer? = null

    override fun startCapture(constraints: VideoConstraints): MediaDeviceInfo {
        check(videoCapturer == null) { "Camera video capturer already started." }

        val device = selectDeviceByIdOrPosition(constraints)
        this.constraints = constraints.copy(isFrontFacing = device.isFrontFacing)

        val size = selectVideoSize(device.deviceId, constraints)
        val fps = selectFps(device.deviceId, constraints)
        videoCapturer = enumerator.createCapturer(device.deviceId, CameraEventsHandler()).apply {
            initialize(
                surfaceTextureHelper,
                applicationContext,
                videoSource.native.capturerObserver
            )
            startCapture(size.width, size.height, fps)
        }

        return device
    }

    private fun selectDeviceByIdOrPosition(constraints: VideoConstraints): MediaDeviceInfo {
        val deviceId = constraints.deviceId
        val isFrontFacing = constraints.isFrontFacing

        val searchCriteria: (String) -> Boolean = when {
            deviceId != null -> {
                { it == deviceId }
            }

            isFrontFacing != null -> {
                { enumerator.isFrontFacing(it) == isFrontFacing }
            }

            else -> {
                { true }
            }
        }

        val device = enumerator.deviceNames.firstOrNull(searchCriteria)?.let {
            MediaDeviceInfo(
                deviceId = it,
                label = it,
                kind = MediaDeviceKind.VideoInput,
                isFrontFacing = enumerator.isFrontFacing(it)
            )
        }

        return device ?: throw CameraVideoCapturerException.notFound(constraints)
    }

    private fun selectVideoSize(cameraId: String, constraints: VideoConstraints): Size {
        if (constraints.width != null && constraints.height != null) {
            return Size(constraints.width, constraints.height)
        }

        val formats = enumerator.getSupportedFormats(cameraId)
        val sizes = formats.map { Size(it.width, it.height) }
        if (sizes.isEmpty()) throw CameraVideoCapturerException.notFound(constraints)

        return CameraEnumerationAndroid.getClosestSupportedSize(
            sizes,
            DEFAULT_VIDEO_WIDTH,
            DEFAULT_VIDEO_HEIGHT
        )
    }

    private fun selectFps(cameraId: String, constraints: VideoConstraints): Int {
        if (constraints.fps != null) return constraints.fps

        val formats = enumerator.getSupportedFormats(cameraId)
        val framerates = formats.map { it.framerate }
        if (framerates.isEmpty()) throw CameraVideoCapturerException.notFound(constraints)

        val framerateRange = CameraEnumerationAndroid.getClosestSupportedFramerateRange(
            framerates,
            DEFAULT_FRAME_RATE
        )

        return framerateRange.max
    }

    override suspend fun switchCamera(): MediaDeviceInfo {
        val currentPosition = constraints.isFrontFacing
            ?: error("Current camera position is not set!")
        val devices = enumerator.deviceNames
        val deviceId = devices.firstOrNull { enumerator.isFrontFacing(it) == !currentPosition }
            ?: devices.firstOrNull()
            ?: error("There is no any available camera")

        return switchCamera(deviceId)
    }

    override suspend fun switchCamera(cameraId: String): MediaDeviceInfo {
        val isFrontFacing = switchCameraInternal(cameraId)
        constraints = constraints.copy(isFrontFacing = isFrontFacing)

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
            capturer.switchCamera(object : org.webrtc.CameraVideoCapturer.CameraSwitchHandler {
                override fun onCameraSwitchDone(isFrontCamera: Boolean) {
                    cont.resume(isFrontCamera)
                }

                override fun onCameraSwitchError(errorDescription: String) {
                    cont.resumeWithException(CameraVideoCapturerException(errorDescription))
                }
            }, deviceId)
        }
    }

    override fun stopCapture() {
        videoCapturer?.stopCapture()
        videoCapturer = null
    }

    private inner class CameraEventsHandler : org.webrtc.CameraVideoCapturer.CameraEventsHandler {
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
