package com.shepeliev.webrtckmp.media

import com.shepeliev.webrtckmp.ApplicationContextHolder
import com.shepeliev.webrtckmp.VideoTrackConstraints
import org.webrtc.VideoSource

internal class VideoCapturerFactory {

    fun createVideoCapturer(videoSource: VideoSource, constraints: VideoTrackConstraints): VideoCapturer {
        return  CameraVideoCapturer(ApplicationContextHolder.context, videoSource, constraints)
    }
}
