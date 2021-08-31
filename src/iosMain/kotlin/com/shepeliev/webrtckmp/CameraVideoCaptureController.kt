package com.shepeliev.webrtckmp

import WebRTC.RTCCameraVideoCapturer
import WebRTC.RTCVideoCapturerDelegateProtocol
import kotlinx.cinterop.useContents
import platform.AVFoundation.*
import platform.CoreMedia.CMFormatDescriptionGetMediaSubType
import platform.CoreMedia.CMVideoFormatDescriptionGetDimensions
import kotlin.math.abs

internal class CameraVideoCaptureController(private val constraints: VideoTrackConstraints) {
    private var videoCapturer: RTCCameraVideoCapturer? = null
    private var position: AVCaptureDevicePosition = AVCaptureDevicePositionBack
    private lateinit var delegate: RTCVideoCapturerDelegateProtocol
    private lateinit var device: AVCaptureDevice
    private lateinit var format: AVCaptureDeviceFormat
    private var fps: Long = -1

    fun initialize(delegate: RTCVideoCapturerDelegateProtocol) {
        this.delegate = delegate
    }

    fun startCapture() {
        if (videoCapturer != null) return
        videoCapturer = RTCCameraVideoCapturer(delegate)
        if (!this::device.isInitialized) selectDevice()
        selectFormat()
        selectFps()
        videoCapturer?.startCaptureWithDevice(device, format, fps)
    }

    fun stopCapture() {
        videoCapturer?.stopCapture()
        videoCapturer = null
    }

    private fun selectDevice() {
        val deviceId = constraints.deviceId
        val isFrontFacing = constraints.facingMode?.exact == FacingMode.User ||
            constraints.facingMode?.ideal == FacingMode.User
        position = when (isFrontFacing) {
            true -> AVCaptureDevicePositionFront
            false -> AVCaptureDevicePositionBack
        }

        val searchCriteria: (Any?) -> Boolean = when {
            deviceId != null -> {
                { (it as AVCaptureDevice).uniqueID == deviceId }
            }
            else -> {
                { (it as AVCaptureDevice).position == position }
            }
        }

        device = RTCCameraVideoCapturer.captureDevices()
            .firstOrNull(searchCriteria) as? AVCaptureDevice
            ?: throw CameraVideoCapturerException.notFound(constraints)
    }

    private fun selectFormat() {
        val targetWidth = constraints.width?.exact
            ?: constraints.height?.exact
            ?: DEFAULT_VIDEO_WIDTH
        val targetHeight = constraints.height?.exact
            ?: constraints.height?.ideal
            ?: DEFAULT_VIDEO_HEIGHT
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
        val requestedFps = constraints.frameRate?.exact
            ?: constraints.frameRate?.ideal
            ?: DEFAULT_FRAME_RATE

        val maxSupportedFramerate = format.videoSupportedFrameRateRanges.fold(0.0) { acc, range ->
            val fpsRange = range as AVFrameRateRange
            maxOf(acc, fpsRange.maxFrameRate)
        }

        fps = minOf(maxSupportedFramerate, requestedFps.toDouble()).toLong()
    }

    fun switchCamera() {
        checkNotNull(videoCapturer) { "Video capturing is not started." }
        stopCapture()
        position = if (position == AVCaptureDevicePositionFront) {
            AVCaptureDevicePositionBack
        } else {
            AVCaptureDevicePositionFront
        }
        device = RTCCameraVideoCapturer.captureDevices()
            .firstOrNull { (it as AVCaptureDevice).position == position } as? AVCaptureDevice
            ?: run {
                val facingMode = when (position) {
                    AVCaptureDevicePositionBack -> FacingMode.Environment
                    AVCaptureDevicePositionFront -> FacingMode.User
                    else -> error("Unknown AVCaptureDevicePosition: $position")
                }
                throw CameraVideoCapturerException.notFound(facingMode)
            }
        startCapture()
    }

    fun switchCamera(deviceId: String) {
        checkNotNull(videoCapturer) { "Video capturing is not started." }
        stopCapture()
        device = RTCCameraVideoCapturer.captureDevices()
            .firstOrNull { (it as AVCaptureDevice).uniqueID == deviceId } as? AVCaptureDevice
            ?: throw CameraVideoCapturerException.notFound(deviceId)
        stopCapture()
    }
}
