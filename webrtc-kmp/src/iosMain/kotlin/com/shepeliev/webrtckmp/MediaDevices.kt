package com.shepeliev.webrtckmp

import WebRTC.RTCCameraVideoCapturer
import WebRTC.RTCMediaConstraints
import platform.AVFoundation.AVCaptureDevice
import platform.Foundation.NSUUID

internal actual val mediaDevices: MediaDevices = MediaDevicesImpl

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
            LocalAudioTrack(track, constraints.audio)
        }

        val videoTrack = constraints.video?.let { videoConstraints ->
            val videoSource = WebRtc.peerConnectionFactory.videoSource()
            val iosVideoTrack =
                WebRtc.peerConnectionFactory.videoTrackWithSource(videoSource, NSUUID.UUID().UUIDString())
            val videoCaptureController = CameraVideoCaptureController(videoConstraints, videoSource)
            LocalVideoTrack(iosVideoTrack, videoCaptureController)
        }

        return MediaStream(listOfNotNull(audioTrack, videoTrack))
    }

    override suspend fun getDisplayMedia(
        token: ScreenCaptureToken?,
        streamConstraints: (MediaStreamConstraintsBuilder.() -> Unit)?,
    ): MediaStream {
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

actual typealias ScreenCaptureToken = Any
