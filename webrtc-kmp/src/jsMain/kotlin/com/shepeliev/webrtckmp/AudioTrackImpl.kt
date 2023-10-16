package com.shepeliev.webrtckmp

import org.w3c.dom.mediacapture.MediaStreamTrack as JsMediaStreamTrack

internal class AudioTrackImpl(
    js: JsMediaStreamTrack
) : MediaStreamTrackImpl(js), AudioTrack
