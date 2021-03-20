package com.shepeliev.webrtckmm

import cocoapods.GoogleWebRTC.RTCCameraVideoCapturer
import com.shepeliev.webrtckmm.utils.uuid
import kotlinx.cinterop.useContents
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceFormat
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDevicePositionFront
import platform.AVFoundation.AVFrameRateRange
import platform.AVFoundation.position
import platform.CoreMedia.CMFormatDescriptionGetMediaSubType
import platform.CoreMedia.CMVideoFormatDescriptionGetDimensions
import platform.Foundation.NSLog
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.abs

@ThreadLocal
actual object MediaDevices {
    private const val tag = "MediaDevices"

    private var audioSource: AudioSource? = null
    private var videoSource: VideoSource? = null
    private var cameraCapturer: RTCCameraVideoCapturer? = null
    private val audioTracks = mutableMapOf<String, Unit>()
    private val videoTracks = mutableMapOf<String, Unit>()

    private var isFrontCamera = false

    // TODO implement video constraints
    @Throws(CameraVideoCapturerException::class, CancellationException::class)
    actual suspend fun getUserMedia(audio: Boolean, video: Boolean): MediaStream {
        val factory = peerConnectionFactory
        var audioTrack: AudioTrack? = null
        if (audio) {
            val source = audioSource ?: factory.createAudioSource(mediaConstraints())
            audioSource = source
            audioTrack = factory.createAudioTrack(uuid(), source)
            audioTracks += audioTrack.id to Unit
        }

        var videoTrack: VideoTrack? = null
        if (video) {
            val source = videoSource ?: factory.createVideoSource(
                isScreencast = false,
                alignTimestamps = true
            )
            videoSource = source
            if (cameraCapturer == null) {
                isFrontCamera = false
                val device = selectDeviceByPosition(isFrontCamera)
                cameraCapturer = RTCCameraVideoCapturer(source.native)
                startVideoCapture(device, 1280, 720, 30.0)
            }
            videoTrack = factory.createVideoTrack(uuid(), source)
            videoTracks += videoTrack.id to Unit
        }

        val nativeStream = factory.native.mediaStreamWithStreamId(uuid())
        return MediaStream(nativeStream).apply {
            audioTrack?.let { addTrack(it) }
            videoTrack?.let { addTrack(it) }
        }
    }

    private fun startVideoCapture(
        device: AVCaptureDevice,
        width: Int,
        height: Int,
        maxFps: Double
    ) {
        val format = selectFormatForDevice(device, width, height)
        val fps = selectFpsForFormat(format, maxFps)
        cameraCapturer?.startCaptureWithDevice(device, format, fps)
    }

    private fun selectDeviceByPosition(isFrontCamera: Boolean): AVCaptureDevice {
        val position = if (isFrontCamera) {
            AVCaptureDevicePositionFront
        } else {
            AVCaptureDevicePositionBack
        }
        val device = RTCCameraVideoCapturer.captureDevices().firstOrNull {
            (it as AVCaptureDevice).position == position
        } as AVCaptureDevice?

        return device
            ?: throw CameraVideoCapturerException("Requested camera not found. {isFrontCamera = $isFrontCamera}")
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
            } else if (diff == currentDiff && pixelFormat == cameraCapturer?.preferredOutputPixelFormat()) {
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

    @Throws(CameraVideoCapturerException::class, CancellationException::class)
    actual suspend fun switchCamera(): MediaDeviceInfo {
        val capturer =
            cameraCapturer ?: throw CameraVideoCapturerException("Camera video capturer is stopped")

        isFrontCamera = !isFrontCamera
        capturer.stopCapture()
        val device = selectDeviceByPosition(isFrontCamera)
        startVideoCapture(device, 1280, 720, 30.0)

        return MediaDeviceInfo(
            deviceId = device.uniqueID,
            label = device.localizedName,
            kind = MediaDeviceKind.VideoInput,
            isFrontFacing = device.position == AVCaptureDevicePositionFront
        )
    }

    @Throws(CameraVideoCapturerException::class, CancellationException::class)
    actual suspend fun switchCamera(cameraId: String): MediaDeviceInfo {
        val capturer = cameraCapturer
            ?: throw CameraVideoCapturerException("Camera video capturer is stopped")

        val device = selectDeviceById(cameraId)
            ?: throw CameraVideoCapturerException("Camera ID: $cameraId not found")

        capturer.stopCapture()
        startVideoCapture(device, 1280, 720, 30.0)
        isFrontCamera = device.position == AVCaptureDevicePositionFront

        return MediaDeviceInfo(
            deviceId = device.uniqueID,
            label = device.localizedName,
            kind = MediaDeviceKind.VideoInput,
            isFrontFacing = device.position == AVCaptureDevicePositionFront
        )
    }

    private fun selectDeviceById(id: String): AVCaptureDevice? {
        return RTCCameraVideoCapturer.captureDevices().firstOrNull {
            val device = it as AVCaptureDevice
            device.uniqueID == id
        } as AVCaptureDevice?
    }

    internal fun onAudioTrackStopped(trackId: String) {
        if (audioTracks.remove(trackId) == null) return

        if (audioTracks.isEmpty()) {
            NSLog("$tag: There is no any active audio track. Dispose audio source.")
            audioSource = null
        }
    }

    internal fun onVideoTrackStopped(trackId: String) {
        if (videoTracks.remove(trackId) == null) return

        if (videoTracks.isEmpty()) {
            cameraCapturer?.stopCapture()
            cameraCapturer = null
            videoSource = null
        }
    }
}