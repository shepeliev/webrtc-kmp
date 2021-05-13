package com.shepeliev.webrtckmp

import org.webrtc.VideoTrack as NativeVideoTrack

actual class VideoTrack internal constructor(
    override val native: NativeVideoTrack,
) : BaseMediaStreamTrack(), MediaStreamTrack
