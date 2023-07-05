package com.shepeliev.webrtckmp

internal interface VideoCaptureController {
    val settings: MediaTrackSettings
    fun startCapture()
    fun stopCapture()
}
