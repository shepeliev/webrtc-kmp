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

        var audioTrack: AudioTrack? = null
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
            val audioSource = WebRtc.peerConnectionFactory.createAudioSource(mediaConstraints)
            val androidTrack = WebRtc.peerConnectionFactory.createAudioTrack(
                UUID.randomUUID().toString(),
                audioSource
            )
            audioTrack = LocalAudioTrack(androidTrack, audioSource, constraints.audio)
        }

        var videoTrack: LocalVideoTrack? = null
        if (constraints.video != null) {
            checkCameraPermission()
            val videoSource = WebRtc.peerConnectionFactory.createVideoSource(false)
            val videoCaptureController = CameraVideoCaptureController(
                constraints.video,
                videoSource
            )
            val androidTrack = WebRtc.peerConnectionFactory.createVideoTrack(
                UUID.randomUUID().toString(),
                videoSource
            )
            videoTrack = LocalVideoTrack(androidTrack, videoCaptureController)
        }

        return MediaStream(listOfNotNull(audioTrack, videoTrack))
    }

    override suspend fun getDisplayMedia(): MediaStream {
        TODO("Not yet implemented for Android platform")
    }

    override suspend fun supportsDisplayMedia(): Boolean = false

    private fun checkRecordAudioPermission() {
        val result = ContextCompat.checkSelfPermission(
            ApplicationContextHolder.context,
            Manifest.permission.RECORD_AUDIO
        )
        if (result != PackageManager.PERMISSION_GRANTED) throw RecordAudioPermissionException()
    }

    private fun checkCameraPermission() {
        val result = ContextCompat.checkSelfPermission(
            ApplicationContextHolder.context,
            Manifest.permission.CAMERA
        )
        if (result != PackageManager.PERMISSION_GRANTED) throw CameraPermissionException()
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
