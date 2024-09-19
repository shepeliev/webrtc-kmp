package com.shepeliev.webrtckmp

import android.content.Context
import android.media.projection.MediaProjection
import android.util.DisplayMetrics
import android.view.WindowManager
import org.webrtc.ScreenCapturerAndroid
import org.webrtc.Size
import org.webrtc.VideoCapturer
import org.webrtc.VideoSource

internal class ScreenCapturerController(
    videoSource: VideoSource,
) : VideoCapturerController(videoSource) {

    override fun createVideoCapturer(): VideoCapturer {
        return ScreenCapturerAndroid(
            MediaProjectionIntentHolder.intent,
            object : MediaProjection.Callback() {}
        )
    }

    @Suppress("DEPRECATION")
    override fun selectVideoSize(): Size {
        val displayMetrics = DisplayMetrics()
        val windowsManager = WebRtc.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowsManager.defaultDisplay.getRealMetrics(displayMetrics)
        return Size(displayMetrics.widthPixels, displayMetrics.heightPixels)
    }

    override fun selectFps(): Int {
        return DEFAULT_FRAME_RATE
    }
}
