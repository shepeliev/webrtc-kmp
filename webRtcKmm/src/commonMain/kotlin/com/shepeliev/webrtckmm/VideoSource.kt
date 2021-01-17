package com.shepeliev.webrtckmm

expect class VideoSource : MediaSource {
    override val state: MediaSource.State
    val capturerObserver: CapturerObserver

    fun setIsScreencast(isScreencast: Boolean)
    fun adaptOutputFormat(width: Int, height: Int, fps: Int)

    fun adaptOutputFormat(
        landscapeWidth: Int,
        landscapeHeight: Int,
        portraitWidth: Int,
        portraitHeight: Int,
        fps: Int
    )

    fun adaptOutputFormat(
        targetLandscapeAspectRatio: AspectRatio,
        targetPortraitAspectRatio: AspectRatio,
        maxLandscapePixelCount: Int? = null,
        maxPortraitPixelCount: Int? = null,
        maxFps: Int? = null
    )

    fun setVideoProcessor(videoProcessor: VideoProcessor?)
    override fun dispose()
}

data class AspectRatio(val width: Int, val height: Int)
