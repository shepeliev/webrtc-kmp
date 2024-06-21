package com.shepeliev.webrtckmp.externals

internal external interface RTCTrackEvent {
    val receiver: RTCRtpReceiver
    val track: PlatformMediaStreamTrack
    val transceiver: RTCRtpTransceiver
}

internal expect val RTCTrackEvent.streams: List<PlatformMediaStream>
