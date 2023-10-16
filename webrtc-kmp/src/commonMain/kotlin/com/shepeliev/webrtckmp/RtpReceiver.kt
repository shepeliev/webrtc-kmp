package com.shepeliev.webrtckmp

expect class RtpReceiver {
    val id: String
    val track: MediaStreamTrack?
    val parameters: RtpParameters

    fun getCapabilities(kind: MediaStreamTrackKind): RtpCapabilities?
}
