package com.shepeliev.webrtckmp

internal interface VideoCapturerController {
    val settings: MediaTrackSettings
    fun startCapture()
    fun stopCapture()
}
