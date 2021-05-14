package com.shepeliev.webrtckmp

import WebRTC.RTCCameraVideoCapturer
import kotlinx.cinterop.useContents
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceFormat
import platform.CoreMedia.CMFormatDescriptionGetMediaSubType
import platform.CoreMedia.CMVideoFormatDescriptionGetDimensions
import kotlin.math.abs

internal actual class CameraVideoCapturer actual constructor() {

    private var capturer: RTCCameraVideoCapturer? = null
    private lateinit var videoSource: VideoSource
    private lateinit var currentCameraId: String
    private lateinit var videoConstraints: VideoConstraints

    actual fun startCapture(
        cameraId: String,
        constraints: VideoConstraints,
        videoSource: VideoSource
    ) {
        check(capturer == null) { "Camera video capturer already started." }
        currentCameraId = cameraId
        this.videoConstraints = constraints
        this.videoSource = videoSource

        capturer = RTCCameraVideoCapturer(videoSource.nativeCapturerObserver)

        val device = RTCCameraVideoCapturer.captureDevices()
            .map { it as AVCaptureDevice }
            .first { it.uniqueID == cameraId }


        val width = constraints.width ?: DEFAULT_VIDEO_WIDTH
        val height = constraints.height ?: DEFAULT_VIDEO_HEIGHT
        val fps = constraints.fps ?: DEFAULT_FRAME_RATE
        val format = selectFormatForDevice(device, width, height)

        capturer!!.startCaptureWithDevice(device, format, fps.toLong())
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
            } else if (diff == currentDiff && pixelFormat == capturer!!.preferredOutputPixelFormat()) {
                return@fold Pair(currentDiff, format)
            }
            Pair(0, currentFormat)
        }.second

        return format
            ?: throw CameraVideoCapturerException(
                "No valid video format for device $device. Requested video frame size: ${targetWidth}x$targetHeight"
            )
    }

    actual suspend fun switchCamera(): MediaDeviceInfo {
        val devices = CameraEnumerator.enumerateDevices()
        val currentCamera = devices.first { it.deviceId == currentCameraId }
        val nextCamera =
            devices.firstOrNull { it.isFrontFacing != currentCamera.isFrontFacing } ?: currentCamera
        return switchCamera(nextCamera)
    }

    actual suspend fun switchCamera(cameraId: String): MediaDeviceInfo {
        val camera = CameraEnumerator.enumerateDevices().firstOrNull { it.deviceId == cameraId }
            ?: throw CameraVideoCapturerException.notFound(cameraId)
        return switchCamera(camera)
    }

    private fun switchCamera(camera: MediaDeviceInfo): MediaDeviceInfo {
        checkNotNull(capturer) { "Video capturing is not started" }
        stopCapture()
        startCapture(camera.deviceId, videoConstraints, videoSource)
        return camera
    }


    actual fun stopCapture() {
        capturer?.stopCapture()
        capturer = null
    }
}
