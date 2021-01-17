package com.shepeliev.webrtckmm

interface CapturerObserver {
    fun onCapturerStarted(success: Boolean)
    fun onCapturerStopped()
    fun onFrameCaptured(frame: VideoFrame)
}
