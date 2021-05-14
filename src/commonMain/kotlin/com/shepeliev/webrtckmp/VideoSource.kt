package com.shepeliev.webrtckmp

expect class VideoSource : MediaSource {
    override val state: MediaSource.State

    fun adaptOutputFormat(width: Int, height: Int, fps: Int)

    fun dispose()
}
