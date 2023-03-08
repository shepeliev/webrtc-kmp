package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.media.MediaDevices as NativeMediaDevices

internal actual val mediaDevices: MediaDevices
    get() = MediaDevicesImpl()

class MediaDevicesImpl : MediaDevices {

    override suspend fun getUserMedia(streamConstraints: MediaStreamConstraintsBuilder.() -> Unit): MediaStream {
        TODO("Not yet implemented for JVM platform")
    }

    override suspend fun getDisplayMedia(): MediaStream {
        TODO("Not yet implemented for JVM platform")
    }

    override suspend fun supportsDisplayMedia(): Boolean = false

    override suspend fun enumerateDevices(): List<MediaDeviceInfo> {
        val nativeAudioDevices = NativeMediaDevices.getAudioCaptureDevices()
        val nativeVideoDevices = NativeMediaDevices.getVideoCaptureDevices()

        val allDevices = nativeAudioDevices + nativeVideoDevices

        return allDevices.map {
            MediaDeviceInfo(
                deviceId = it.name,
                label = it.descriptor,
                kind = if (nativeAudioDevices.contains(it)) {
                    MediaDeviceKind.AudioInput
                } else if (nativeVideoDevices.contains(it)) {
                    MediaDeviceKind.VideoInput
                } else {
                    error("Unknown device type!")
                },
            )
        }
    }
}
