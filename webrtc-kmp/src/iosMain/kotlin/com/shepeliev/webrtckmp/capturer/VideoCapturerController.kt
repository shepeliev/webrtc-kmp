package com.shepeliev.webrtckmp.capturer

import com.shepeliev.webrtckmp.MediaTrackSettings

internal abstract class VideoCapturerController {
    var settings: MediaTrackSettings = MediaTrackSettings()
        protected set

    abstract fun startCapture()
    abstract fun stopCapture()
}
