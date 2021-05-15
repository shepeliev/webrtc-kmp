package com.shepeliev.webrtckmp

internal expect class PeerConnectionFactory {
    fun createVideoSource(isScreencast: Boolean = false, alignTimestamps: Boolean = true): VideoSource
    fun createVideoTrack(id: String, videoSource: VideoSource): VideoStreamTrack
    fun createAudioSource(constraints: MediaConstraints): AudioSource
    fun createAudioTrack(id: String, audioSource: AudioSource): AudioStreamTrack
    fun startAecDump(filePath: String, fileSizeLimitBytes: Int)
    fun stopAecDump()
}
