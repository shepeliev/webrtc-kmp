package com.shepeliev.webrtckmp

import WebRTC.RTCVideoTrack

actual class VideoTrack internal constructor(
    override val native: RTCVideoTrack
) : BaseMediaStreamTrack(), MediaStreamTrack
