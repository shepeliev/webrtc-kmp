package com.shepeliev.webrtckmm

expect class VideoSource : MediaSource {
    override val state: MediaSource.State
    val capturerObserver: CapturerObserver

    fun setIsScreencast(isScreencast: Boolean)
    fun adaptOutputFormat(width: Int, height: Int, fps: Int)
    override fun dispose()
}

data class AspectRatio(val width: Int, val height: Int)
