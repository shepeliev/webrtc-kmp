package com.shepeliev.webrtckmp

import org.w3c.dom.mediacapture.MediaStreamTrack as JsMediaStreamTrack

actual class VideoStreamTrack internal constructor(js: JsMediaStreamTrack) : MediaStreamTrack(js)
