package com.shepeliev.webrtckmm

import org.webrtc.VideoTrack as NativeVideoTrack

actual class VideoTrack internal constructor(override val native: NativeVideoTrack):
    BaseMediaStreamTrack(), MediaStreamTrack {

    private val sinks: MutableMap<VideoRenderer, CommonVideoSinkAdapter> = mutableMapOf()

    actual fun addSink(renderer: VideoRenderer) {
        val proxy = CommonVideoSinkAdapter(renderer)
        sinks += renderer to proxy
        native.addSink(proxy)
    }

    actual fun removeSink(renderer: VideoRenderer) {
        val proxy = sinks.remove(renderer)
        proxy?.let { native.removeSink(it) }
    }
}

internal fun NativeVideoTrack.asCommon() = VideoTrack(this)
