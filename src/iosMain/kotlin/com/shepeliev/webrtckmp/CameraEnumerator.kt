package com.shepeliev.webrtckmp

import WebRTC.RTCCameraVideoCapturer
import kotlinx.cinterop.useContents
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceFormat
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDevicePositionFront
import platform.AVFoundation.AVFrameRateRange
import platform.AVFoundation.position
import platform.CoreMedia.CMFormatDescriptionGetMediaSubType
import platform.CoreMedia.CMVideoFormatDescriptionGetDimensions
import kotlin.math.abs

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

    actual fun createCameraVideoCapturer(source: VideoSource): CameraVideoCapturer {
        return CameraVideoCapturerImpl(source)
    }
}

private class CameraVideoCapturerImpl(source: VideoSource) : CameraVideoCapturer {

    private val capturer = RTCCameraVideoCapturer(source.nativeCapturerObserver)
    private lateinit var constraints: VideoConstraints

    override fun startCapture(constraints: VideoConstraints): MediaDeviceInfo {
        val device = selectDeviceByIdOrPosition(constraints)
        this.constraints = constraints.copy(
            isFrontFacing = device.position == AVCaptureDevicePositionFront
        )
        val format = selectFormatForDevice(
            device,
            constraints.width ?: 1280,
            constraints.height ?: 720
        )
        val fps = selectFpsForFormat(format, constraints.fps?.toDouble() ?: 30.0)
        capturer.startCaptureWithDevice(device, format, fps)

        return MediaDeviceInfo(
            deviceId = device.uniqueID,
            label = device.localizedName,
            kind = MediaDeviceKind.VideoInput,
            isFrontFacing = device.position == AVCaptureDevicePositionFront
        )
    }

    override suspend fun switchCamera(): MediaDeviceInfo {
        capturer.stopCapture()
        val currentPosition = constraints.isFrontFacing
            ?: error("Current camera position is not set!")
        return startCapture(constraints.copy(isFrontFacing = !currentPosition))
    }

    override suspend fun switchCamera(cameraId: String): MediaDeviceInfo {
        capturer.stopCapture()
        return startCapture(constraints.copy(deviceId = cameraId))
    }

    private fun selectDeviceByIdOrPosition(constraints: VideoConstraints): AVCaptureDevice {
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

    private fun selectFormatForDevice(
        device: AVCaptureDevice,
        targetWidth: Int,
        targetHeight: Int,
    ): AVCaptureDeviceFormat {
        val formats = RTCCameraVideoCapturer.supportedFormatsForDevice(device)

        val format = formats.fold(Pair(Int.MAX_VALUE, null as AVCaptureDeviceFormat?)) { acc, fmt ->
            val format = fmt as AVCaptureDeviceFormat
            val (currentDiff, currentFormat) = acc

            var diff = currentDiff
            CMVideoFormatDescriptionGetDimensions(format.formatDescription).useContents {
                diff = abs(targetWidth - width) + abs(targetHeight - height)
            }
            val pixelFormat = CMFormatDescriptionGetMediaSubType(format.formatDescription)
            if (diff < currentDiff) {
                return@fold Pair(diff, format)
            } else if (diff == currentDiff && pixelFormat == capturer.preferredOutputPixelFormat()) {
                return@fold Pair(currentDiff, format)
            }
            Pair(0, currentFormat)
        }.second

        return format
            ?: throw CameraVideoCapturerException(
                "No valid video format for device $device. Requested video frame size: ${targetWidth}x$targetHeight"
            )
    }

    private fun selectFpsForFormat(format: AVCaptureDeviceFormat, maxFps: Double): Long {
        val maxSupportedFramerate = format.videoSupportedFrameRateRanges.fold(0.0) { acc, range ->
            val fpsRange = range as AVFrameRateRange
            maxOf(acc, fpsRange.maxFrameRate)
        }
        return minOf(maxSupportedFramerate, maxFps).toLong()
    }

    override fun stopCapture() {
        capturer.stopCapture()
    }
}
