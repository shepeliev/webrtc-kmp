@file:JvmName("AndroidMediaDevices")

package com.shepeliev.webrtckmp

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import org.webrtc.Camera2Enumerator
import org.webrtc.MediaConstraints
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
            val mediaConstraints = MediaConstraints().apply {
                mandatory.addAll(
                    constraints.audio.toMandatoryMap()
                        .map { (k, v) -> MediaConstraints.KeyValuePair("$k", "$v") }
                )
                optional.addAll(
                    constraints.audio.toOptionalMap()
                        .map { (k, v) -> MediaConstraints.KeyValuePair("$k", "$v") }
                )
            }
            val audioSource = peerConnectionFactory.createAudioSource(mediaConstraints)
            val androidTrack = peerConnectionFactory.createAudioTrack(UUID.randomUUID().toString(), audioSource)
            audioTrack = AudioStreamTrack(
                android = androidTrack,
            ) { audioSource.dispose() }
        }

        var videoTrack: VideoStreamTrack? = null
        if (constraints.video != null) {
            checkCameraPermission()
            val videoCaptureController = CameraVideoCaptureController(constraints.video)
            val videoSource = peerConnectionFactory.createVideoSource(videoCaptureController.isScreencast)
            videoCaptureController.initialize(videoSource.capturerObserver)
            val androidTrack = peerConnectionFactory.createVideoTrack(UUID.randomUUID().toString(), videoSource)
            videoTrack = VideoStreamTrack(
                android = androidTrack,
                onSwitchCamera = { deviceId ->
                    deviceId?.let { videoCaptureController.switchCamera(it) } ?: videoCaptureController.switchCamera()
                },
            ) {
                videoCaptureController.stopCapture()
                videoSource.dispose()
            }
            videoCaptureController.startCapture()
        }

        val localMediaStream = peerConnectionFactory.createLocalMediaStream(UUID.randomUUID().toString())
        return MediaStream(localMediaStream).apply {
            if (audioTrack != null) addTrack(audioTrack)
            if (videoTrack != null) addTrack(videoTrack)
        }
    }

    override suspend fun getDisplayMedia(): MediaStream {
        TODO("Not yet implemented for Android platform")
    }

    override suspend fun supportsDisplayMedia(): Boolean = false

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
