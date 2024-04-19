@file:OptIn(ExperimentalForeignApi::class)

package com.shepeliev.webrtckmp

import WebRTC.RTCCameraVideoCapturer
import WebRTC.RTCVideoSource
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceFormat
import platform.AVFoundation.AVCaptureDevicePosition
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDevicePositionFront
import platform.AVFoundation.AVFrameRateRange
import platform.AVFoundation.position
import platform.CoreMedia.CMFormatDescriptionGetMediaSubType
import platform.CoreMedia.CMVideoFormatDescriptionGetDimensions
import kotlin.math.abs

internal class CameraVideoCaptureController(
    private val constraints: MediaTrackConstraints,
    private val videoSource: RTCVideoSource,
) : VideoCaptureController {
    private var videoCapturer: RTCCameraVideoCapturer? = null
    private var position: AVCaptureDevicePosition = AVCaptureDevicePositionBack
    private lateinit var device: AVCaptureDevice
    private lateinit var format: AVCaptureDeviceFormat
    private var fps: Long = -1

    override var settings: MediaTrackSettings = MediaTrackSettings()
        private set

    override fun startCapture() {
        if (videoCapturer != null) return
        videoCapturer = RTCCameraVideoCapturer(videoSource)
        if (!this::device.isInitialized) selectDevice()
        selectFormat()
        selectFps()

        var width: Int? = null
        var height: Int? = null
        CMVideoFormatDescriptionGetDimensions(format.formatDescription).useContents {
            width = this.width
            height = this.height
        }

        settings = settings.copy(
            deviceId = device.uniqueID,
            facingMode = device.position.toFacingMode(),
            width = width,
            height = height,
            frameRate = fps.toDouble()
        )

        videoCapturer?.startCaptureWithDevice(device, format, fps)
    }

    override fun stopCapture() {
        videoCapturer?.stopCapture()
        videoCapturer = null
    }

    private fun selectDevice() {
        position = when (constraints.facingMode?.value) {
            FacingMode.User -> AVCaptureDevicePositionFront
            FacingMode.Environment -> AVCaptureDevicePositionBack
            null -> AVCaptureDevicePositionFront
        }

        val searchCriteria: (Any?) -> Boolean = when {
            constraints.deviceId != null -> {
                { (it as AVCaptureDevice).uniqueID == constraints.deviceId }
            }

            else -> {
                { (it as AVCaptureDevice).position == position }
            }
        }

        device = RTCCameraVideoCapturer.captureDevices()
            .firstOrNull(searchCriteria) as? AVCaptureDevice
            ?: throw CameraVideoCapturerException.notFound(constraints)

        settings = settings.copy(
            deviceId = device.uniqueID,
            facingMode = device.position.toFacingMode()
        )
    }

    private fun selectFormat() {
        val targetWidth = constraints.width?.value ?: DEFAULT_VIDEO_WIDTH
        val targetHeight = constraints.height?.value ?: DEFAULT_VIDEO_HEIGHT
        val formats = RTCCameraVideoCapturer.supportedFormatsForDevice(device)

        format = formats.fold(Pair(Int.MAX_VALUE, null as AVCaptureDeviceFormat?)) { acc, fmt ->
            val format = fmt as AVCaptureDeviceFormat
            val (currentDiff, currentFormat) = acc

            var diff = currentDiff
            CMVideoFormatDescriptionGetDimensions(format.formatDescription).useContents {
                diff = abs(targetWidth - width) + abs(targetHeight - height)
            }
            val pixelFormat = CMFormatDescriptionGetMediaSubType(format.formatDescription)
            if (diff < currentDiff) {
                return@fold Pair(diff, format)
            } else if (diff == currentDiff && pixelFormat == videoCapturer!!.preferredOutputPixelFormat()) {
                return@fold Pair(currentDiff, format)
            }
            Pair(0, currentFormat)
        }.second ?: throw CameraVideoCapturerException(
            "No valid video format for device $device. Requested video frame size: ${targetWidth}x$targetHeight"
        )
    }

    private fun selectFps() {
        val requestedFps = constraints.frameRate?.value ?: DEFAULT_FRAME_RATE

        val maxSupportedFrameRate = format.videoSupportedFrameRateRanges.fold(0.0) { acc, range ->
            val fpsRange = range as AVFrameRateRange
            maxOf(acc, fpsRange.maxFrameRate)
        }

        fps = minOf(maxSupportedFrameRate, requestedFps.toDouble()).toLong()
    }

    fun switchCamera() {
        checkNotNull(videoCapturer) { "Video capturing is not started." }
        val captureDevices = RTCCameraVideoCapturer.captureDevices()
        if (captureDevices.size < 2) {
            throw CameraVideoCapturerException("No other camera device found.")
        }

        stopCapture()
        val deviceIndex = captureDevices.indexOfFirst {
            (it as AVCaptureDevice).uniqueID == device.uniqueID
        }
        device = captureDevices[(deviceIndex + 1) % captureDevices.size] as AVCaptureDevice
        startCapture()

        settings = settings.copy(
            deviceId = device.uniqueID,
            facingMode = device.position.toFacingMode()
        )
    }

    fun switchCamera(deviceId: String) {
        checkNotNull(videoCapturer) { "Video capturing is not started." }

        stopCapture()
        device = RTCCameraVideoCapturer.captureDevices()
            .firstOrNull { (it as AVCaptureDevice).uniqueID == deviceId } as? AVCaptureDevice
            ?: throw CameraVideoCapturerException.notFound(deviceId)
        startCapture()

        settings = settings.copy(
            deviceId = device.uniqueID,
            facingMode = device.position.toFacingMode()
        )
    }

    private fun AVCaptureDevicePosition.toFacingMode(): FacingMode? {
        return when (this) {
            AVCaptureDevicePositionFront -> FacingMode.User
            AVCaptureDevicePositionBack -> FacingMode.Environment
            else -> null
        }
    }
}
