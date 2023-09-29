package com.shepeliev.webrtckmp

internal const val DEFAULT_VIDEO_WIDTH = 1280
internal const val DEFAULT_VIDEO_HEIGHT = 720
internal const val DEFAULT_FRAME_RATE = 30

interface MediaDevices {
    suspend fun getUserMedia(streamConstraints: MediaStreamConstraintsBuilder.() -> Unit = {}): MediaStream

    suspend fun getUserMedia(audio: Boolean = false, video: Boolean = false): MediaStream {
        return getUserMedia {
            if (audio) audio()
            if (video) video()
        }
    }

    suspend fun getDisplayMedia(
        token: ScreenCaptureToken? = null,
        streamConstraints: (MediaStreamConstraintsBuilder.() -> Unit)? = null,
    ): MediaStream

    suspend fun supportsDisplayMedia(): Boolean

    suspend fun enumerateDevices(): List<MediaDeviceInfo>

    companion object : MediaDevices by mediaDevices
}

expect class ScreenCaptureToken

internal expect val mediaDevices: MediaDevices

internal fun MediaTrackConstraints.toMandatoryMap(): Map<Any?, *> {
    return mutableMapOf<Any?, String>().apply {
        echoCancellation?.exact?.let { this += "googEchoCancellation" to "$it" }
        autoGainControl?.exact?.let { this += "googAutoGainControl" to "$it" }
        noiseSuppression?.exact?.let { this += "googNoiseSuppression" to "$it" }
        highpassFilter?.exact?.let { this += "googHighpassFilter" to "$it" }
        typingNoiseDetection?.exact?.let { this += "googTypingNoiseDetection" to "$it" }
    }
}

internal fun MediaTrackConstraints.toOptionalMap(): Map<Any?, *> {
    return mutableMapOf<Any?, String>().apply {
        echoCancellation?.ideal?.let { this += "googEchoCancellation" to "$it" }
        autoGainControl?.ideal?.let { this += "googAutoGainControl" to "$it" }
        noiseSuppression?.ideal?.let { this += "googNoiseSuppression" to "$it" }
        highpassFilter?.exact?.let { this += "googHighpassFilter" to "$it" }
        typingNoiseDetection?.exact?.let { this += "googTypingNoiseDetection" to "$it" }
    }
}
