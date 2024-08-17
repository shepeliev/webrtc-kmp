package com.shepeliev.webrtckmp

import WebRTC.RTCCameraVideoCapturer
import WebRTC.RTCMediaConstraints
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureDevice
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
            val audioSource = WebRtc.peerConnectionFactory.audioSourceWithConstraints(mediaConstraints)
            val track = WebRtc.peerConnectionFactory.audioTrackWithSource(audioSource, NSUUID.UUID().UUIDString())
            LocalAudioStreamTrack(track, constraints.audio)
        }

        val videoTrack = constraints.video?.let { videoConstraints ->
            val videoSource = WebRtc.peerConnectionFactory.videoSource()
            val videoProcessor = WebRtc.videoProcessorFactory?.createVideoProcessor(videoSource)
            val iosVideoTrack = WebRtc.peerConnectionFactory.videoTrackWithSource(
                source = videoSource,
                trackId = NSUUID.UUID().UUIDString()
            )
            val videoCaptureController = CameraVideoCaptureController(
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
        return RTCCameraVideoCapturer.captureDevices().map {
            val device = it as AVCaptureDevice
            MediaDeviceInfo(
                deviceId = device.uniqueID,
                label = device.localizedName,
                kind = MediaDeviceKind.VideoInput
            )
        }
    }
}
