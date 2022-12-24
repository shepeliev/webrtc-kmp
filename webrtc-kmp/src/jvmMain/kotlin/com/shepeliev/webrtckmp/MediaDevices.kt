@file:JvmName("JVMMediaDevices")

package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.media.audio.AudioOptions
import dev.onvoid.webrtc.media.video.VideoDesktopSource
import dev.onvoid.webrtc.media.video.VideoTrackSource
import java.util.UUID

internal actual val mediaDevices: MediaDevices = MediaDevicesImpl

private object MediaDevicesImpl : MediaDevices {

    override suspend fun getUserMedia(streamConstraints: MediaStreamConstraintsBuilder.() -> Unit): MediaStream {
        val constraints = MediaStreamConstraintsBuilder().let {
            streamConstraints(it)
            it.constraints
        }

        var audioTrack: AudioStreamTrack? = null
        if (constraints.audio != null) {
            checkRecordAudioPermission()
            val audioSource = WebRtc.peerConnectionFactory.createAudioSource(AudioOptions().apply {
                this.echoCancellation = constraints.audio.echoCancellation?.exact == true
                this.autoGainControl = constraints.audio.autoGainControl?.exact == true
                this.noiseSuppression = constraints.audio.noiseSuppression?.exact == true
            })
            val androidTrack = WebRtc.peerConnectionFactory.createAudioTrack(UUID.randomUUID().toString(), audioSource)
            audioTrack = AudioStreamTrack(androidTrack, audioSource)
        }

        var videoTrack: VideoStreamTrack? = null
        if (constraints.video != null) {
            checkCameraPermission()
            val videoDesktopSource = VideoDesktopSource().apply {
                constraints.video.frameRate?.exact?.let {
                    this.setFrameRate(it.toInt())
                }
                this.setMaxFrameSize(constraints.video.width.exact,constraints.video.height.exact)
            } // TODO ?
            val videoCaptureController = CameraVideoCaptureController(constraints.video, videoDesktopSource)
            val androidTrack = WebRtc.peerConnectionFactory.createVideoTrack(UUID.randomUUID().toString(), videoDesktopSource)
            videoTrack = VideoStreamTrack(androidTrack, videoCaptureController)
        }

        val localMediaStream = WebRtc.peerConnectionFactory.createLocalMediaStream(UUID.randomUUID().toString())
        return MediaStream(localMediaStream).apply {
            if (audioTrack != null) addTrack(audioTrack)
            if (videoTrack != null) addTrack(videoTrack)
        }
    }

    override suspend fun getDisplayMedia(): MediaStream {
        TODO("Not yet implemented for JVM platform")
    }

    override suspend fun supportsDisplayMedia(): Boolean = false

    private fun checkRecordAudioPermission() {
        val result = ContextCompat.checkSelfPermission(
            ApplicationContextHolder.context,
            Manifest.permission.RECORD_AUDIO
        )
        if (result == PackageManager.PERMISSION_DENIED) throw RecordAudioPermissionException()
    }

    private fun checkCameraPermission() {
        val result = ContextCompat.checkSelfPermission(
            ApplicationContextHolder.context,
            Manifest.permission.CAMERA
        )
        if (result == PackageManager.PERMISSION_DENIED) throw CameraPermissionException()
    }

    override suspend fun enumerateDevices(): List<MediaDeviceInfo> {
        val enumerator = Camera2Enumerator(ApplicationContextHolder.context)
        return enumerator.deviceNames.map {
            MediaDeviceInfo(
                deviceId = it,
                label = it,
                kind = MediaDeviceKind.VideoInput
            )
        }
    }
}
