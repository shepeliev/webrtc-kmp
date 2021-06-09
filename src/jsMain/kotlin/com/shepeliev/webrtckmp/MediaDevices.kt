package com.shepeliev.webrtckmp

import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.dom.mediacapture.AUDIOINPUT
import org.w3c.dom.mediacapture.MediaStreamConstraints
import org.w3c.dom.mediacapture.VIDEOINPUT
import org.w3c.dom.mediacapture.MediaDeviceKind.Companion as JsMediaDeviceKind

actual object MediaDevices {
    actual suspend fun getUserMedia(audio: Boolean, video: Boolean): MediaStream {
        val constraints = MediaStreamConstraints(video, audio)
        val jsStream = window.navigator.mediaDevices.getUserMedia(constraints).await()
        return MediaStream(jsStream)
    }

    actual suspend fun enumerateDevices(): List<MediaDeviceInfo> {
        val devices = window.navigator.mediaDevices.enumerateDevices().await()
        return devices.map {
            val kind = when (it.kind) {
                JsMediaDeviceKind.AUDIOINPUT -> MediaDeviceKind.AudioInput
                JsMediaDeviceKind.VIDEOINPUT -> MediaDeviceKind.VideoInput
                else -> error("Unknown media device kind: ${it.kind}")
            }
            MediaDeviceInfo(
                deviceId = it.deviceId,
                label = it.label,
                kind = kind,
                isFrontFacing = false
            )
        }
    }

    actual suspend fun switchCamera(): MediaDeviceInfo {
        TODO("not implemented yet")
    }

    actual suspend fun switchCamera(cameraId: String): MediaDeviceInfo {
        TODO("not implemented yet")
    }
}
