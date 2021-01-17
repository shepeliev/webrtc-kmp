package com.shepeliev.webrtckmm

import org.webrtc.VideoTrack as NativeVideoTrack

actual class VideoTrack internal constructor(override val  native: NativeVideoTrack):
    BaseMediaStreamTrack(), MediaStreamTrack {

    private val sinks: MutableMap<VideoSink, CommonVideoSinkAdapter> = mutableMapOf()

    actual fun addSink(sink: VideoSink) {
        val proxy = CommonVideoSinkAdapter(sink)
        sinks += sink to proxy
        native.addSink(proxy)
    }

    actual fun removeSink(sink: VideoSink) {
        val proxy = sinks.remove(sink)
        proxy?.let { native.removeSink(it) }
    }
}
