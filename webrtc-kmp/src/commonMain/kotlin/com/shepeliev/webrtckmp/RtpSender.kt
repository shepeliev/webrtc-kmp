package com.shepeliev.webrtckmp

public expect class RtpSender {
    public val id: String
    public val track: MediaStreamTrack?
    public var parameters: RtpParameters
    public val dtmf: DtmfSender?

    public suspend fun replaceTrack(track: MediaStreamTrack?)
}
