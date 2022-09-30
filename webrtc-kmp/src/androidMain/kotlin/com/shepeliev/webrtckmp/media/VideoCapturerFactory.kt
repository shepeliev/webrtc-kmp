package com.shepeliev.webrtckmp.media

import com.shepeliev.webrtckmp.ApplicationContextHolder
import com.shepeliev.webrtckmp.VideoTrackConstraints
import org.webrtc.VideoSource

internal class VideoCapturerFactory {

    fun createVideoCapturer(videoSource: VideoSource, constraints: VideoTrackConstraints): VideoCapturer {
        return when (constraints.deviceId) {
            "color-bars" -> ColorBarsVideoCapturer(ApplicationContextHolder.context, videoSource, constraints)
            else -> CameraVideoCapturer(ApplicationContextHolder.context, videoSource, constraints)
        }
    }
}
