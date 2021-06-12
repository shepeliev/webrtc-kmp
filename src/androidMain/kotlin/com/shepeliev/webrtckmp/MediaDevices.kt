package com.shepeliev.webrtckmp

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import org.webrtc.Camera2Enumerator
import org.webrtc.MediaConstraints
import java.util.UUID

internal object MediaDevicesImpl : MediaDevices {

    override suspend fun getUserMedia(streamConstraints: MediaStreamConstraintsBuilder.() -> Unit): MediaStream {
        val constraints = MediaStreamConstraintsBuilder().let {
            streamConstraints(it)
            it.constraints
        }

        var audioTrack: AudioStreamTrack? = null
        if (constraints.audio != null) {
            checkRecordAudioPermission()
            val mediaConstraints = MediaConstraints().apply {
                mandatory.addAll(
                    constraints.audio.toMandatoryMap()
                        .map { (k, v) -> MediaConstraints.KeyValuePair("$k", "$v") })
                optional.addAll(
                    constraints.audio.toOptionalMap()
                        .map { (k, v) -> MediaConstraints.KeyValuePair("$k", "$v") })
            }
            val audioSource = peerConnectionFactory.createAudioSource(mediaConstraints)
            val track =
                peerConnectionFactory.createAudioTrack(UUID.randomUUID().toString(), audioSource)
            audioTrack = AudioStreamTrack(track, audioSource)
        }

        var videoTrack: VideoStreamTrack? = null
        if (constraints.video != null) {
            checkCameraPermission()
            val videoCaptureController = CameraVideoCaptureController(constraints.video)
            val videoSource =
                peerConnectionFactory.createVideoSource(videoCaptureController.isScreencast)
            videoCaptureController.initialize(videoSource.capturerObserver)
            val track =
                peerConnectionFactory.createVideoTrack(UUID.randomUUID().toString(), videoSource)
            videoTrack = VideoStreamTrack(track, videoSource, videoCaptureController)
            videoCaptureController.startCapture()
        }

        val localMediaStream =
            peerConnectionFactory.createLocalMediaStream(UUID.randomUUID().toString())
        return MediaStream(localMediaStream).apply {
            if (audioTrack != null) addTrack(audioTrack)
            if (videoTrack != null) addTrack(videoTrack)
        }
    }

    private fun checkRecordAudioPermission() {
        val result = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.RECORD_AUDIO
        )
        if (result == PackageManager.PERMISSION_DENIED) throw RecordAudioPermissionException()
    }

    private fun checkCameraPermission() {
        val result = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.CAMERA
        )
        if (result == PackageManager.PERMISSION_DENIED) throw CameraPermissionException()
    }

    override suspend fun enumerateDevices(): List<MediaDeviceInfo> {
        val enumerator = Camera2Enumerator(applicationContext)
        return enumerator.deviceNames.map {
            MediaDeviceInfo(
                deviceId = it,
                label = it,
                kind = MediaDeviceKind.VideoInput
            )
        }
    }
}
