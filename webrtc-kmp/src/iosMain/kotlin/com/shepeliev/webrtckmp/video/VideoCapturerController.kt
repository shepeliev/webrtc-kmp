package com.shepeliev.webrtckmp.video

import com.shepeliev.webrtckmp.MediaTrackSettings

internal interface VideoCapturerController {
    val settings: MediaTrackSettings
    fun startCapture()
    fun stopCapture()
}
