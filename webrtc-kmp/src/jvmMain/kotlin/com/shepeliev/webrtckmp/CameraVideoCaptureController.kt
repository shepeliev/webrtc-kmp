package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.media.MediaDevices
import dev.onvoid.webrtc.media.video.VideoCapture
import dev.onvoid.webrtc.media.video.VideoDevice
import kotlin.math.abs

internal class CameraVideoCaptureController(
    private val constraints: VideoTrackConstraints,
) : VideoCaptureController() {

    private var currentDevice: VideoDevice? = null

    override fun createVideoCapture(): VideoCapture {
        selectDevice()
        return VideoCapture().apply { setVideoCaptureDevice(currentDevice) }
    }

    private fun selectDevice() {
        val devices = MediaDevices.getVideoCaptureDevices()

        currentDevice = devices.firstOrNull { it.name == constraints.deviceId }
            ?: throw CameraVideoCapturerException.notFound(constraints)
    }

    override fun selectVideoSize(): Size {
        val requestedWidth = constraints.width?.exact
            ?: constraints.width?.ideal
            ?: DEFAULT_VIDEO_WIDTH

        val requestedHeight = constraints.height?.exact
            ?: constraints.height?.ideal
            ?: DEFAULT_VIDEO_HEIGHT

        val capabilities = MediaDevices.getVideoCaptureCapabilities(currentDevice)

        val sizes = capabilities?.map { Size(it.width, it.height) } ?: emptyList()
        if (sizes.isEmpty()) throw CameraVideoCapturerException.notFound(constraints)

        return Size(
            width = sizes.map { it.width }.closestValue(requestedWidth),
            height = sizes.map { it.height }.closestValue(requestedHeight)
        )
    }

    override fun selectFps(): Int {
        val requestedFps = constraints.frameRate?.exact
            ?: constraints.frameRate?.ideal
            ?: DEFAULT_FRAME_RATE

        val capabilities = MediaDevices.getVideoCaptureCapabilities(currentDevice)

        val frameRates = capabilities?.map { it.frameRate } ?: emptyList()
        if (frameRates.isEmpty()) throw CameraVideoCapturerException.notFound(constraints)

        return frameRates.closestValue(requestedFps.toInt())
    }

    fun switchCamera() {
        val devices = MediaDevices.getVideoCaptureDevices()
        videoCapturer.setVideoCaptureDevice(devices.first { it != currentDevice })
    }

    fun switchCamera(deviceName: String) {
        val devices = MediaDevices.getVideoCaptureDevices()
        videoCapturer.setVideoCaptureDevice(devices.first { it.name == deviceName })
    }
}

private fun List<Int>.closestValue(value: Int) = minBy { abs(value - it) }
