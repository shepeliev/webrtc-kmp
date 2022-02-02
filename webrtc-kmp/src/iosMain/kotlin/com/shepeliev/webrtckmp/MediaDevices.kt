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
            val audioSource = factory.audioSourceWithConstraints(mediaConstraints)
            val track = factory.audioTrackWithSource(audioSource, NSUUID.UUID().UUIDString())
            AudioStreamTrack(track)
        }

        val videoTrack = constraints.video?.let { videoConstraints ->
            CameraVideoCaptureController(videoConstraints).let { videoCaptureController ->
                val videoSource = factory.videoSource()
                val iosVideoTrack = factory.videoTrackWithSource(videoSource, NSUUID.UUID().UUIDString())
                val videoTrack = VideoStreamTrack(
                    ios = iosVideoTrack,
                    onSwitchCamera = { deviceId: String? ->
                        deviceId?.let { videoCaptureController.switchCamera(it) } ?: videoCaptureController.switchCamera()

                    }
                ) { videoCaptureController.stopCapture() }
                videoCaptureController.initialize(videoSource)
                videoCaptureController.startCapture()
                videoTrack
            }
        }

        val localMediaStream = factory.mediaStreamWithStreamId(NSUUID.UUID().UUIDString())
        return MediaStream(localMediaStream).apply {
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
