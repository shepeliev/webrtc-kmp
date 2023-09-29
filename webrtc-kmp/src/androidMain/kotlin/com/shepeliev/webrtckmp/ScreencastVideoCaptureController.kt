package com.shepeliev.webrtckmp

import android.content.Intent
import android.media.projection.MediaProjection
import org.webrtc.ScreenCapturerAndroid
import org.webrtc.VideoCapturer
import org.webrtc.VideoSource

internal class ScreencastVideoCaptureController(
    videoSource: VideoSource,
    constraints: MediaTrackConstraints?,
    private val screenCaptureToken: Intent,
) : VideoCaptureController(videoSource, ScreencastVideoCaptureHelper.buildMediaTrackSettings(constraints)) {
    override fun createVideoCapturer(): VideoCapturer {
        return ScreenCapturerAndroid(screenCaptureToken, MediaProjectionCallback())
    }

    private inner class MediaProjectionCallback : MediaProjection.Callback() {
        override fun onStop() {
            videoCapturerStopListener.onStop("MediaProjectionCallback.onStop")
        }
    }
}

private object ScreencastVideoCaptureHelper {
    fun buildMediaTrackSettings(constraints: MediaTrackConstraints?): MediaTrackSettings {
        return MediaTrackSettings(
            deviceId = null,
            facingMode = null,
            width = constraints?.width?.value ?: DEFAULT_VIDEO_WIDTH,
            height = constraints?.height?.value ?: DEFAULT_VIDEO_HEIGHT,
            frameRate = constraints?.frameRate?.value?.toDouble() ?: DEFAULT_FRAME_RATE.toDouble(),
        )
    }
}
