package com.shepeliev.webrtckmp

import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.dom.mediacapture.AUDIOINPUT
import org.w3c.dom.mediacapture.VIDEOINPUT
import kotlin.js.Json
import kotlin.js.json
import org.w3c.dom.mediacapture.MediaDeviceKind.Companion as JsMediaDeviceKind
import org.w3c.dom.mediacapture.MediaStreamConstraints as JsMediaStreamConstraints

internal actual val mediaDevices: MediaDevices = MediaDevicesImpl

private object MediaDevicesImpl : MediaDevices {
    override suspend fun getUserMedia(streamConstraints: MediaStreamConstraintsBuilder.() -> Unit): MediaStream {
        val constraints = MediaStreamConstraintsBuilder().let {
            streamConstraints(it)
            it.constraints
        }
        val jsStream = window.navigator.mediaDevices.getUserMedia(constraints.toJson()).await()
        return MediaStream(jsStream)
    }

    override suspend fun getDisplayMedia(): MediaStream {
        if (!supportsDisplayMedia()) {
            error("getDisplayMedia is not supported in this environment")
        }

        val jsStream = window.navigator.mediaDevices.getDisplayMedia().await()
        return MediaStream(jsStream)
    }

    override suspend fun supportsDisplayMedia(): Boolean = window.navigator.mediaDevices.supportsDisplayMedia()

    override suspend fun enumerateDevices(): List<MediaDeviceInfo> {
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
                kind = kind
            )
        }
    }

    private fun MediaStreamConstraints.toJson(): JsMediaStreamConstraints {
        val a: dynamic = audio?.let {
            if (audio.deviceId == null &&
                audio.groupId == null &&
                audio.autoGainControl == null &&
                audio.channelCount == null &&
                audio.echoCancellation == null &&
                audio.latency == null &&
                audio.noiseSuppression == null &&
                audio.sampleRate == null
            ) {
                return@let true
            }

            json().apply {
                audio.deviceId?.also { add(json("deviceId" to it)) }
                audio.groupId?.also { add(json("groupId" to it)) }
                audio.autoGainControl?.also { add(json("autoGainControl" to it.toJson())) }
                audio.channelCount?.also { add(json("channelCount" to it.toJson())) }
                audio.echoCancellation?.also { add(json("echoCancellation" to it.toJson())) }
                audio.latency?.also { add(json("latency" to it.toJson())) }
                audio.noiseSuppression?.also { add(json("noiseSuppression" to it.toJson())) }
                audio.sampleRate?.also { add(json("sampleRate" to it.toJson())) }
            }
        }

        val v: dynamic = video?.let {
            if (video.deviceId == null &&
                video.groupId == null &&
                video.facingMode == null &&
                video.aspectRatio == null &&
                video.width == null &&
                video.height == null &&
                video.frameRate == null
            ) {
                return@let true
            }

            json().apply {
                video.deviceId?.also { add(json("deviceId" to it)) }
                video.groupId?.also { add(json("groupId" to it)) }
                video.facingMode?.also { add(json("facingMode" to it.toJson())) }
                video.aspectRatio?.also { add(json("aspectRatio" to it.toJson())) }
                video.width?.also { add(json("width" to it.toJson())) }
                video.height?.also { add(json("height" to it.toJson())) }
                video.frameRate?.also { add(json("frameRate" to it.toJson())) }
            }
        }

        return JsMediaStreamConstraints(v, a)
    }

    private fun <T> ValueOrConstrain<T>.toJson(): Json {
        val values = mutableListOf<Pair<String, Any>>().apply {
            exact?.also { add(Pair("exact", it)) }
            ideal?.also { add(Pair("ideal", it)) }
        }.toTypedArray()

        return json(*values)
    }
}
