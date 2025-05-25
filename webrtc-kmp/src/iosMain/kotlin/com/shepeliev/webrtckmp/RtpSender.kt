@file:OptIn(ExperimentalForeignApi::class)

package com.shepeliev.webrtckmp

import WebRTC.RTCRtpSender
import kotlinx.cinterop.ExperimentalForeignApi

public actual class RtpSender(
    public val ios: RTCRtpSender,
    track: MediaStreamTrack?,
) {
    public actual val id: String get() = ios.senderId()

    private var _track: MediaStreamTrack? = track
    public actual val track: MediaStreamTrack? get() = _track

    public actual var parameters: RtpParameters
        get() = RtpParameters(ios.parameters)
        set(value) {
            ios.parameters = value.native
        }

    public actual val dtmf: DtmfSender? get() = ios.dtmfSender?.let { DtmfSender(it) }

    public actual suspend fun replaceTrack(track: MediaStreamTrack?) {
        ios.setTrack((track as? MediaStreamTrackImpl)?.ios)
        _track = track
    }
}
