package com.shepeliev.webrtckmp

import com.shepeliev.webrtckmp.externals.PlatformMediaStream
import com.shepeliev.webrtckmp.internal.await
import com.shepeliev.webrtckmp.internal.toList
import kotlinx.browser.window
import org.w3c.dom.mediacapture.AUDIOINPUT
import org.w3c.dom.mediacapture.MediaTrackConstraints
import org.w3c.dom.mediacapture.VIDEOINPUT
import kotlin.js.Promise

internal actual val mediaDevices: MediaDevices = MediaDevicesImpl

private object MediaDevicesImpl : MediaDevices {
    override suspend fun getUserMedia(streamConstraints: MediaStreamConstraintsBuilder.() -> Unit): MediaStream {
        val constraints = MediaStreamConstraintsBuilder().let {
            streamConstraints(it)
            it.constraints
        }
        val jsStream = window.navigator.mediaDevices.getUserMedia(constraints.toJson()).await<PlatformMediaStream>()
        return MediaStream(jsStream)
    }

    override suspend fun getDisplayMedia(): MediaStream {
        if (!supportsDisplayMedia()) {
            error("getDisplayMedia is not supported in this environment")
        }

        return MediaStream(jsGetDisplayMedia().await())
    }

    override suspend fun supportsDisplayMedia(): Boolean = jsSupportsDisplayMedia()

    override suspend fun enumerateDevices(): List<MediaDeviceInfo> {
        val devices = window.navigator.mediaDevices.enumerateDevices()
            .await<JsArray<org.w3c.dom.mediacapture.MediaDeviceInfo>>()
            .toList()
            .filterNotNull()
        return devices.map {
            val kind = when (it.kind) {
                org.w3c.dom.mediacapture.MediaDeviceKind.AUDIOINPUT -> MediaDeviceKind.AudioInput
                org.w3c.dom.mediacapture.MediaDeviceKind.VIDEOINPUT -> MediaDeviceKind.VideoInput
                else -> error("Unknown media device kind: ${it.kind}")
            }
            MediaDeviceInfo(
                deviceId = it.deviceId,
                label = it.label,
                kind = kind
            )
        }
    }

    private fun MediaStreamConstraints.toJson(): org.w3c.dom.mediacapture.MediaStreamConstraints {
        val audio = audio?.let {
            jsMediaTrackConstraints(
                exactDeviceId = it.deviceId,
                exactGroupId = it.groupId,
            )
        }

        val video = video?.let {
            jsMediaTrackConstraints(
                exactDeviceId = it.deviceId,
                exactGroupId = it.groupId,
                exactFacingMode = it.facingMode?.exact?.name?.lowercase(),
                idealFacingMode = it.facingMode?.ideal?.name?.lowercase(),
                exactAspectRatio = it.aspectRatio?.exact,
                idealAspectRatio = it.aspectRatio?.ideal,
                exactWidth = it.width?.exact,
                idealWidth = it.width?.ideal,
                exactHeight = it.height?.exact,
                idealHeight = it.height?.ideal,
                exactFrameRate = it.frameRate?.exact,
                idealFrameRate = it.frameRate?.ideal
            )
        }

        return jsMediaStreamConstraints(audio, video)
    }
}

private fun jsGetDisplayMedia(): Promise<org.w3c.dom.mediacapture.MediaStream> =
    js("navigator.mediaDevices.getDisplayMedia()")

private fun jsSupportsDisplayMedia(): Boolean = js("navigator.mediaDevices.supportsDisplayMedia()")

@Suppress("UNUSED_PARAMETER")
private fun jsMediaStreamConstraints(audio: JsAny?, video: JsAny?): org.w3c.dom.mediacapture.MediaStreamConstraints =
    js("({ audio: audio, video: video })")

@Suppress("UNUSED_PARAMETER")
private fun jsMediaTrackConstraints(
    exactDeviceId: String? = null,
    idealDeviceId: String? = null,
    exactGroupId: String? = null,
    idealGroupId: String? = null,
    exactFacingMode: String? = null,
    idealFacingMode: String? = null,
    exactAspectRatio: Double? = null,
    idealAspectRatio: Double? = null,
    exactWidth: Int? = null,
    idealWidth: Int? = null,
    exactHeight: Int? = null,
    idealHeight: Int? = null,
    exactFrameRate: Double? = null,
    idealFrameRate: Double? = null,
): MediaTrackConstraints = js(
    """
    var constraints = {};
    if (exactDeviceId) {
        constraints.deviceId = { exact: exactDeviceId };
    }
    if (idealDeviceId) {
        constraints.deviceId = { ideal: idealDeviceId };
    }
    if (exactGroupId) {
        constraints.groupId = { exact: exactGroupId };
    }
    if (idealGroupId) {
        constraints.groupId = { ideal: idealGroupId };
    }
    if (exactFacingMode) {
        constraints.facingMode = { exact: exactFacingMode };
    }
    if (idealFacingMode) {
        constraints.facingMode = { ideal: idealFacingMode };
    }
    if (exactAspectRatio) {
        constraints.aspectRatio = { exact: exactAspectRatio };
    }
    if (idealAspectRatio) {
        constraints.aspectRatio = { ideal: idealAspectRatio };
    }
    if (exactWidth) {
        constraints.width = { exact: exactWidth };
    }
    if (idealWidth) {
        constraints.width = { ideal: idealWidth };
    }
    if (exactHeight) {
        constraints.height = { exact: exactHeight };
    }
    if (idealHeight) {
        constraints.height = { ideal: idealHeight };
    }
    if (exactFrameRate) {
        constraints.frameRate = { exact: exactFrameRate };
    }
    if (idealFrameRate) {
        constraints.frameRate = { ideal: idealFrameRate };
    }
    
    return constraints;
"""
)
