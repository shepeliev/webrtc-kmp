package com.shepeliev.webrtckmp

import org.webrtc.VideoTrack as AndroidVideoTrack

actual class VideoTrack internal constructor(
    override val native: AndroidVideoTrack,
) : BaseMediaStreamTrack(), MediaStreamTrack
