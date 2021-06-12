package com.shepeliev.webrtckmp

class TrackEvent internal constructor(
    val receiver: RtpReceiver,
    val streams: List<String>,
    val track: MediaStreamTrack?,
    val transceiver: RtpTransceiver
)
