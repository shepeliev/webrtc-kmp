package com.shepeliev.webrtckmp

import org.w3c.dom.mediacapture.MediaStreamTrack as JsMediaStreamTrack

internal class AudioStreamTrackImpl(
    js: JsMediaStreamTrack
) : MediaStreamTrackImpl(js), AudioStreamTrack
