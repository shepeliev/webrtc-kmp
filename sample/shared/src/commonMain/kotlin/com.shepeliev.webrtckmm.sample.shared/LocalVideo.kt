package com.shepeliev.webrtckmm.sample.shared

import com.shepeliev.webrtckmm.MediaDevices
import com.shepeliev.webrtckmm.MediaStream
import com.shepeliev.webrtckmm.VideoRenderer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

interface LocalVideoListener {
    fun onVideoStarted()
    fun onError(description: String?)
}

class LocalVideo(private val listener: LocalVideoListener) : CoroutineScope by CommonMainScope() {

    var videoRenderer: VideoRenderer? = null
        set(value) {
            if (field != null) {
                mediaStream?.videoTracks?.forEach { it.removeSink(field!!) }
            }

            if (value != null) {
                mediaStream?.videoTrack()?.addSink(value)
            }

            field = value
        }

    private var mediaStream: MediaStream? = null

    fun startVideo() {
        stopVideo()

        launch {
            try {
                MediaDevices.getUserMedia(audio = true, video = true)
            } catch (e: Throwable) {
                listener.onError(e.message)
                null
            }?.also { stream ->
                mediaStream = stream
                videoRenderer?.also { renderer ->
                    stream.videoTrack()?.addSink(renderer)
                }
                listener.onVideoStarted()
            }
        }
    }

    fun switchCamera() {
        launch {
            try {
                MediaDevices.switchCamera()
            } catch (e: Throwable) {
                listener.onError(e.message)
            }
        }
    }

    fun stopVideo() {
        val stream = mediaStream ?: return
        stream.videoTracks.forEach { track ->
            videoRenderer?.let { sink -> track.removeSink(sink) }
            track.stop()
        }
    }
}