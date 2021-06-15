package com.shepeliev.webrtckmp

const val DEFAULT_VIDEO_WIDTH = 1280
const val DEFAULT_VIDEO_HEIGHT = 720
const val DEFAULT_FRAME_RATE = 30

interface MediaDevices {
    suspend fun getUserMedia(streamConstraints: MediaStreamConstraintsBuilder.() -> Unit = {}): MediaStream
    suspend fun enumerateDevices(): List<MediaDeviceInfo>
}

fun MediaTrackConstraints.toMandatoryMap(): Map<Any?, *> {
    return mutableMapOf<Any?, String>().apply {
        echoCancellation?.exact?.let { this += "googEchoCancellation" to "$it" }
        autoGainControl?.exact?.let { this += "googAutoGainControl" to "$it" }
        noiseSuppression?.exact?.let { this += "googNoiseSuppression" to "$it" }
    }
}

fun MediaTrackConstraints.toOptionalMap(): Map<Any?, *> {
    return mutableMapOf<Any?, String>().apply {
        echoCancellation?.ideal?.let { this += "googEchoCancellation" to "$it" }
        autoGainControl?.ideal?.let { this += "googAutoGainControl" to "$it" }
        noiseSuppression?.ideal?.let { this += "googNoiseSuppression" to "$it" }
    }
}
