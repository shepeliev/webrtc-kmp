package com.shepeliev.webrtckmp

import WebRTC.RTCCameraVideoCapturer
import WebRTC.RTCMediaConstraints
import platform.AVFoundation.AVCaptureDevice
import platform.Foundation.NSUUID

internal object MediaDevicesImpl : MediaDevices {
    override suspend fun getUserMedia(streamConstraints: MediaStreamConstraintsBuilder.() -> Unit): MediaStream {
        val constraints = MediaStreamConstraintsBuilder().let {
            streamConstraints(it)
            it.constraints
        }

        var audioTrack: AudioStreamTrack? = null
        if (constraints.audio != null) {
            val mediaConstraints = RTCMediaConstraints(
                mandatoryConstraints = constraints.audio.toMandatoryMap(),
                optionalConstraints = constraints.audio.toOptionalMap()
            )
            val audioSource = factory.audioSourceWithConstraints(mediaConstraints)
            val track = factory.audioTrackWithSource(audioSource, NSUUID.UUID().UUIDString())
            audioTrack = AudioStreamTrack(track)
        }

        var videoTrack: VideoStreamTrack? = null
        if (constraints.video != null) {
            val videoCaptureController = CameraVideoCaptureController(constraints.video)
            val videoSource = factory.videoSource()
            videoCaptureController.initialize(videoSource)
            val track = factory.videoTrackWithSource(videoSource, NSUUID.UUID().UUIDString())
            videoTrack = VideoStreamTrack(track, videoCaptureController)
            videoCaptureController.startCapture()
        }

        val localMediaStream = factory.mediaStreamWithStreamId(NSUUID.UUID().UUIDString())
        return MediaStream(localMediaStream).apply {
            if (audioTrack != null) addTrack(audioTrack)
            if (videoTrack != null) addTrack(videoTrack)
        }
    }

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
