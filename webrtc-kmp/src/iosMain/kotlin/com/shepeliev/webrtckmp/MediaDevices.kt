@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

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
            LocalAudioStreamTrack(track, constraints.audio)
        }

        val videoTrack = constraints.video?.let { videoConstraints ->
            val videoSource = WebRtc.peerConnectionFactory.videoSource()
            val iosVideoTrack = WebRtc.peerConnectionFactory.videoTrackWithSource(videoSource, NSUUID.UUID().UUIDString())
            val videoCaptureController = CameraVideoCaptureController(videoConstraints, videoSource)
            LocalVideoStreamTrack(iosVideoTrack, videoCaptureController)
        }

        val localMediaStream = WebRtc.peerConnectionFactory.mediaStreamWithStreamId(NSUUID.UUID().UUIDString())
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
