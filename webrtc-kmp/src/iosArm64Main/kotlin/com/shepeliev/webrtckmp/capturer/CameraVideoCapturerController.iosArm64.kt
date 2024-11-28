package com.shepeliev.webrtckmp.capturer

import WebRTC.RTCCameraVideoCapturer
import WebRTC.RTCLogEx
import WebRTC.RTCLoggingSeverity
import WebRTC.RTCVideoCapturerDelegateProtocol
import com.shepeliev.webrtckmp.CameraVideoCapturerException
import com.shepeliev.webrtckmp.DEFAULT_FRAME_RATE
import com.shepeliev.webrtckmp.DEFAULT_VIDEO_HEIGHT
import com.shepeliev.webrtckmp.DEFAULT_VIDEO_WIDTH
import com.shepeliev.webrtckmp.FacingMode
import com.shepeliev.webrtckmp.MediaTrackConstraints
import com.shepeliev.webrtckmp.utils.copyContents
import com.shepeliev.webrtckmp.value
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceFormat
import platform.AVFoundation.AVCaptureDevicePosition
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDevicePositionFront
import platform.AVFoundation.AVCaptureMultiCamSession
import platform.AVFoundation.AVFrameRateRange
import platform.AVFoundation.multiCamSupported
import platform.AVFoundation.position
import platform.CoreMedia.CMFormatDescriptionGetMediaSubType
import platform.CoreMedia.CMVideoFormatDescriptionGetDimensions
import kotlin.math.abs

@OptIn(ExperimentalForeignApi::class)
internal actual class CameraVideoCapturerController actual constructor(
    private val constraints: MediaTrackConstraints,
    private val videoCapturerDelegate: RTCVideoCapturerDelegateProtocol
) : VideoCapturerController() {
    private var videoCapturer: RTCCameraVideoCapturer? = null
    private var device: AVCaptureDevice? = null

    actual override fun startCapture() {
        if (videoCapturer != null) return
        videoCapturer = RTCCameraVideoCapturer(videoCapturerDelegate)

        val device = device
            ?: run {
                val position = constraints.facingMode?.value.toAVCaptureDevicePosition()
                selectDevice(position).also { device = it }
            }
            ?: run {
                RTCLogEx(RTCLoggingSeverity.RTCLoggingSeverityWarning, "[$TAG] No capture devices found.")
                return
            }

        val format = selectFormat(
            device = device,
            targetWidth = constraints.width?.value ?: DEFAULT_VIDEO_WIDTH,
            targetHeight = constraints.height?.value ?: DEFAULT_VIDEO_HEIGHT
        ) ?: run {
            RTCLogEx(
                RTCLoggingSeverity.RTCLoggingSeverityWarning,
                "[$TAG] No valid formats for device $device."
            )
            return
        }

        val fps = selectFps(
            format = format,
            targetFps = constraints.frameRate?.value ?: DEFAULT_FRAME_RATE.toDouble()
        )

        val dimensions =
            CMVideoFormatDescriptionGetDimensions(format.formatDescription).copyContents()

        settings = settings.copy(
            deviceId = device.uniqueID,
            facingMode = device.position.toFacingMode(),
            width = dimensions.width,
            height = dimensions.height,
            frameRate = fps
        )

        RTCLogEx(RTCLoggingSeverity.RTCLoggingSeverityInfo, "[$TAG] Start capturing video.")

        videoCapturer?.startCaptureWithDevice(device, format, fps.toLong())
    }

    actual override fun stopCapture() {
        val videoCapturer = videoCapturer ?: return
        this.videoCapturer = null
        RTCLogEx(RTCLoggingSeverity.RTCLoggingSeverityInfo, "[$TAG] Stop capturing video.")
        videoCapturer.stopCapture()
    }

    private fun selectDevice(position: AVCaptureDevicePosition): AVCaptureDevice? {
        val searchCriteria: (Any?) -> Boolean = when {
            constraints.deviceId != null -> {
                { (it as AVCaptureDevice).uniqueID == constraints.deviceId }
            }

            else -> {
                { (it as AVCaptureDevice).position == position }
            }
        }

        val device = RTCCameraVideoCapturer.captureDevices().firstOrNull(searchCriteria)
        return device as? AVCaptureDevice
    }

    private fun selectFormat(
        device: AVCaptureDevice,
        targetWidth: Int,
        targetHeight: Int
    ): AVCaptureDeviceFormat? {
        val formats = RTCCameraVideoCapturer.supportedFormatsForDevice(device)
        var selectedFormat: AVCaptureDeviceFormat? = null
        var currentDiff = Int.MAX_VALUE

        for (format in formats) {
            format as? AVCaptureDeviceFormat ?: continue
            if (format.multiCamSupported != AVCaptureMultiCamSession.multiCamSupported) continue

            val dimensions =
                CMVideoFormatDescriptionGetDimensions(format.formatDescription).copyContents()
            val pixelFormat = CMFormatDescriptionGetMediaSubType(format.formatDescription)
            val diff = abs(targetWidth - dimensions.width) + abs(targetHeight - dimensions.height)
            if (diff < currentDiff) {
                selectedFormat = format
                currentDiff = diff
            } else if (diff == currentDiff && pixelFormat == videoCapturer!!.preferredOutputPixelFormat()) {
                selectedFormat = format
            }
        }

        return selectedFormat
    }

    private fun selectFps(format: AVCaptureDeviceFormat, targetFps: Double): Double {
        val maxSupportedFrameRate = format.videoSupportedFrameRateRanges.maxOf {
            (it as AVFrameRateRange).maxFrameRate
        }
        return targetFps.coerceAtMost(maxSupportedFrameRate)
    }

    actual fun switchCamera() {
        checkNotNull(videoCapturer) { "[$TAG] Video capturing is not started." }
        val captureDevices = RTCCameraVideoCapturer.captureDevices()
        if (captureDevices.size < 2) {
            RTCLogEx(
                RTCLoggingSeverity.RTCLoggingSeverityWarning,
                "[$TAG] No other camera device found."
            )
            return
        }

        stopCapture()
        val deviceIndex = captureDevices.indexOfFirst {
            (it as AVCaptureDevice).uniqueID == device?.uniqueID
        }
        device = captureDevices[(deviceIndex + 1) % captureDevices.size] as AVCaptureDevice
        startCapture()
    }

    actual fun switchCamera(deviceId: String) {
        checkNotNull(videoCapturer) { "[$TAG] Video capturing is not started." }

        stopCapture()
        device = RTCCameraVideoCapturer.captureDevices()
            .firstOrNull { (it as AVCaptureDevice).uniqueID == deviceId } as? AVCaptureDevice
            ?: throw CameraVideoCapturerException.notFound(deviceId)
        startCapture()
    }

    private fun AVCaptureDevicePosition.toFacingMode(): FacingMode? {
        return when (this) {
            AVCaptureDevicePositionFront -> FacingMode.User
            AVCaptureDevicePositionBack -> FacingMode.Environment
            else -> null
        }
    }

    private fun FacingMode?.toAVCaptureDevicePosition(): AVCaptureDevicePosition {
        return when (this) {
            FacingMode.User -> AVCaptureDevicePositionFront
            FacingMode.Environment -> AVCaptureDevicePositionBack
            else -> AVCaptureDevicePositionFront
        }
    }

    companion object {
        private const val TAG = "CameraVideoCapturerController"
    }
}
