package com.shepeliev.webrtckmm

import cocoapods.GoogleWebRTC.RTCVideoTrack

actual class VideoTrack internal constructor(override val native: RTCVideoTrack):
    BaseMediaStreamTrack(), MediaStreamTrack {

    private val sinks: MutableMap<VideoRenderer, CommonVideoSinkAdapter> = mutableMapOf()

    actual fun addSink(renderer: VideoRenderer) {
        val proxy = CommonVideoSinkAdapter(renderer)
        sinks += renderer to proxy
        native.addRenderer(proxy)
    }

    actual fun removeSink(renderer: VideoRenderer) {
        val proxy = sinks.remove(renderer)
        proxy?.let { native.removeRenderer(it) }
    }
}
