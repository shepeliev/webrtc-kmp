package com.shepeliev.webrtckmp.media

import com.shepeliev.webrtckmp.VideoTrackConstraints

internal interface VideoCapturer {
    val isScreencast: Boolean
    val constraints: VideoTrackConstraints

    fun addErrorListener(errorListener: VideoCapturerErrorListener)
    fun startCapture()
    fun stopCapture()
    fun dispose()
}
