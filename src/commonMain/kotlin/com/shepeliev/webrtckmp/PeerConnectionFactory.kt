package com.shepeliev.webrtckmp

internal expect class PeerConnectionFactory {
    fun createLocalMediaStream(id: String): MediaStream
    fun createVideoSource(isScreencast: Boolean = false, alignTimestamps: Boolean = true): VideoSource
    fun createVideoTrack(id: String, videoSource: VideoSource): VideoTrack
    fun createAudioSource(constraints: MediaConstraints): AudioSource
    fun createAudioTrack(id: String, audioSource: AudioSource): AudioTrack
    fun startAecDump(filePath: String, fileSizeLimitBytes: Int)
    fun stopAecDump()
}
