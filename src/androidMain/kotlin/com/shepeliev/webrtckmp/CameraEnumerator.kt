package com.shepeliev.webrtckmp

import org.webrtc.Camera1Enumerator
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraEnumerationAndroid
import org.webrtc.Size

internal actual object CameraEnumerator {
    val enumerator by lazy {
        val context = WebRtcKmp.applicationContext
        if (Camera2Enumerator.isSupported(context)) {
            Camera2Enumerator(context)
        } else {
            Camera1Enumerator()
        }
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

    actual fun selectDevice(constraints: VideoConstraints): MediaDeviceInfo {
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
}
