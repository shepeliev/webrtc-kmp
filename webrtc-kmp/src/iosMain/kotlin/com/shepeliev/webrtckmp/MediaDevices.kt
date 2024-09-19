package com.shepeliev.webrtckmp

import WebRTC.RTCCameraVideoCapturer
import WebRTC.RTCMediaConstraints
import com.shepeliev.webrtckmp.capturer.CameraVideoCapturerController
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureDevice
import platform.Foundation.NSBundle
import platform.Foundation.NSFileManager
import platform.Foundation.NSUUID

internal actual val mediaDevices: MediaDevices = MediaDevicesImpl

@OptIn(ExperimentalForeignApi::class)
private object MediaDevicesImpl : MediaDevices {
    override suspend fun getUserMedia(streamConstraints: MediaStreamConstraintsBuilder.() -> Unit): MediaStream {
        val constraints = MediaStreamConstraintsBuilder().let {
            streamConstraints(it)
            it.constraints
        }

        val audioTrack = constraints.audio?.let { audioConstraints ->
            val mediaConstraints = RTCMediaConstraints(
                mandatoryConstraints = audioConstraints.toMandatoryMap(),
                optionalConstraints = audioConstraints.toOptionalMap()
            )
            val audioSource =
                WebRtc.peerConnectionFactory.audioSourceWithConstraints(mediaConstraints)

            val track = WebRtc.peerConnectionFactory.audioTrackWithSource(
                source = audioSource,
                trackId = NSUUID.UUID().UUIDString()
            )
            LocalAudioStreamTrack(track, constraints.audio)
        }

        val videoTrack = constraints.video?.let { videoConstraints ->
            val videoSource = WebRtc.peerConnectionFactory.videoSource()
            val videoProcessor = WebRtc.videoProcessorFactory?.createVideoProcessor(videoSource)
            val iosVideoTrack = WebRtc.peerConnectionFactory.videoTrackWithSource(
                source = videoSource,
                trackId = NSUUID.UUID().UUIDString()
            )
            val videoCaptureController = CameraVideoCapturerController(
                constraints = videoConstraints,
                videoCapturerDelegate = videoProcessor ?: videoSource
            )
            LocalVideoStreamTrack(iosVideoTrack, videoCaptureController)
        }

        return MediaStream().apply {
            if (audioTrack != null) addTrack(audioTrack)
            if (videoTrack != null) addTrack(videoTrack)
        }
    }

    override suspend fun getDisplayMedia(): MediaStream {
        TODO("Not yet implemented for iOS platform")
    }

    override suspend fun supportsDisplayMedia(): Boolean = false

    override suspend fun enumerateDevices(): List<MediaDeviceInfo> {
        val captureDevices = RTCCameraVideoCapturer.captureDevices().map {
            val device = it as AVCaptureDevice
            MediaDeviceInfo(
                deviceId = device.uniqueID,
                label = device.localizedName,
                kind = MediaDeviceKind.VideoInput
            )
        }
        val fallbackDeviceInfo = getFallbackMediaDeviceInfo()
        return fallbackDeviceInfo?.let { captureDevices + it } ?: captureDevices
    }

    private fun getFallbackMediaDeviceInfo(): MediaDeviceInfo? {
        val nameComponents = WebRtc.simulatorCameraFallbackFileName.split(".")
        if (nameComponents.size != 2) return null

        val path =
            NSBundle.mainBundle.pathForResource(nameComponents[0], nameComponents[1]) ?: return null
        if (!NSFileManager.defaultManager.fileExistsAtPath(path)) return null

        return MediaDeviceInfo(
            deviceId = WebRtc.simulatorCameraFallbackFileName,
            label = "Simulator Camera",
            kind = MediaDeviceKind.VideoInput
        )
    }
}
