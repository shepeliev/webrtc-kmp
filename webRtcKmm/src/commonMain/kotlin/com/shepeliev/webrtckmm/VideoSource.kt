package com.shepeliev.webrtckmm

expect class VideoSource : MediaSource {
    override val state: MediaSource.State

    fun setIsScreencast(isScreencast: Boolean)
    fun adaptOutputFormat(width: Int, height: Int, fps: Int)
}
