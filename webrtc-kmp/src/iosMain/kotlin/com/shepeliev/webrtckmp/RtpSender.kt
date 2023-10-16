package com.shepeliev.webrtckmp

import WebRTC.RTCRtpSender

actual class RtpSender(val ios: RTCRtpSender, track: MediaStreamTrack?) {
    actual val id: String
        get() = ios.senderId()

    private var _track: MediaStreamTrack? = track
    actual val track: MediaStreamTrack? get() = _track

    actual var parameters: RtpParameters
        get() = RtpParameters(ios.parameters)
        set(value) {
            ios.parameters = value.native
        }

    actual val dtmf: DtmfSender?
        get() = ios.dtmfSender?.let { DtmfSender(it) }

    actual fun replaceTrack(track: MediaStreamTrack?) {
        ios.setTrack((track as? MediaStreamTrackImpl)?.native)
        _track = track
    }

    actual fun getCapabilities(kind: MediaStreamTrackKind): RtpCapabilities? {
        require(kind in listOf(MediaStreamTrackKind.Audio, MediaStreamTrackKind.Video)) {
            "Unsupported track kind: $kind"
        }

        return WebRtc.peerConnectionFactory.rtpSenderCapabilitiesFor(kind.asNative()).let {
            // TODO: fill headerExtensions
            RtpCapabilities(it, emptyList())
        }
    }
}
