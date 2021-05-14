package com.shepeliev.webrtckmp

import WebRTC.RTCCameraVideoCapturer
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceFormat
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDevicePositionFront
import platform.AVFoundation.AVFrameRateRange
import platform.AVFoundation.position

internal actual object CameraEnumerator {

    actual suspend fun enumerateDevices(): List<MediaDeviceInfo> {
        return RTCCameraVideoCapturer.captureDevices().map {
            val device = it as AVCaptureDevice
            MediaDeviceInfo(
                deviceId = device.uniqueID,
                label = device.localizedName,
                kind = MediaDeviceKind.VideoInput,
                isFrontFacing = device.position == AVCaptureDevicePositionFront
            )
        }
    }

    actual fun selectDevice(constraints: VideoConstraints): MediaDeviceInfo {
        val device = selectDeviceInternal(constraints)

        return MediaDeviceInfo(
            deviceId = device.uniqueID,
            label = device.localizedName,
            kind = MediaDeviceKind.VideoInput,
            isFrontFacing = device.position == AVCaptureDevicePositionFront
        )
    }

    internal fun selectDeviceInternal(constraints: VideoConstraints): AVCaptureDevice {
        val deviceId = constraints.deviceId
        val position = when (constraints.isFrontFacing) {
            true -> AVCaptureDevicePositionFront
            false -> AVCaptureDevicePositionBack
            else -> null
        }

        val searchCriteria: (Any?) -> Boolean = when {
            deviceId != null -> {
                { (it as AVCaptureDevice).uniqueID == deviceId }
            }
            position != null -> {
                { (it as AVCaptureDevice).position == position }
            }
            else -> {
                { true }
            }
        }

        val device = RTCCameraVideoCapturer.captureDevices().firstOrNull(searchCriteria)

        return device as? AVCaptureDevice
            ?: throw CameraVideoCapturerException.notFound(constraints)
    }

    private fun selectFpsForFormat(format: AVCaptureDeviceFormat, maxFps: Double): Long {
        val maxSupportedFramerate = format.videoSupportedFrameRateRanges.fold(0.0) { acc, range ->
            val fpsRange = range as AVFrameRateRange
            maxOf(acc, fpsRange.maxFrameRate)
        }
        return minOf(maxSupportedFramerate, maxFps).toLong()
    }
}

