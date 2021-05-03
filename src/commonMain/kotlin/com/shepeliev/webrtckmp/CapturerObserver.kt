package com.shepeliev.webrtckmp

interface CapturerObserver {
    fun onCapturerStarted(success: Boolean)
    fun onCapturerStopped()
    fun onFrameCaptured(frame: VideoFrame)
}
