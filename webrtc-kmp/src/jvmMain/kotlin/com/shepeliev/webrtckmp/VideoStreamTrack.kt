package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.media.video.VideoFrame
import dev.onvoid.webrtc.media.video.VideoTrack
import dev.onvoid.webrtc.media.video.VideoTrackSink
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.atomic.AtomicInteger

actual class VideoStreamTrack internal constructor(
    private val videoTrack: VideoTrack,
    private val videoCaptureController: VideoCaptureController? = null,
) : MediaStreamTrack(videoTrack) {

    // Setup track mute detector for remote tracks only.
    private val trackMuteDetector: TrackMuteDetector? =
        if (videoCaptureController == null) TrackMuteDetector() else null

    init {
        videoCaptureController?.startCapture()
        trackMuteDetector?.let {
            addSink(it)
            it.start()
        }
    }

    actual suspend fun switchCamera(deviceId: String?) {
        (videoCaptureController as? CameraVideoCaptureController)?.let { controller ->
            deviceId?.let { controller.switchCamera(it) } ?: controller.switchCamera()
        }
    }

    fun addSink(sink: VideoTrackSink) {
        videoTrack.addSink(sink)
    }

    fun removeSink(sink: VideoTrackSink) {
        videoTrack.removeSink(sink)
    }

    override fun onSetEnabled(enabled: Boolean) {
        if (enabled) {
            videoCaptureController?.startCapture()
            trackMuteDetector?.start()
        } else {
            videoCaptureController?.stopCapture()
            trackMuteDetector?.stop()
        }
    }

    override fun onStop() {
        videoCaptureController?.stopCapture()
        videoCaptureController?.dispose()
        trackMuteDetector?.let {
            removeSink(it)
            it.dispose()
        }
    }

    /**
     * Implements 'mute'/'unmute' events for remote video tracks through the [VideoSink] interface.
     *
     * The original idea is from React Native WebRTC
     * https://github.com/react-native-webrtc/react-native-webrtc/blob/95cf638dfa/jvm/src/main/java/com/oney/WebRTCModule/VideoTrackAdapter.java#L69
     */
    private inner class TrackMuteDetector : VideoTrackSink {
        private val timer: Timer = Timer("VideoTrackMutedTimer")
        private var setMuteTask: TimerTask? = null

        @Volatile
        private var disposed = false
        private val frameCounter: AtomicInteger = AtomicInteger()
        private var mutedState = false

        override fun onVideoFrame(frame: VideoFrame) {
            frameCounter.addAndGet(1)
        }

        fun start() {
            if (disposed) return

            synchronized(this) {
                setMuteTask?.cancel()
                setMuteTask = object : TimerTask() {
                    private var lastFrameNumber: Int = frameCounter.get()

                    override fun run() {
                        if (disposed) return

                        val frameCount = frameCounter.get()
                        val isMuted = lastFrameNumber == frameCount
                        if (isMuted != mutedState) {
                            mutedState = isMuted
                            setMuted(isMuted)
                        }
                        lastFrameNumber = frameCounter.get()
                    }
                }
                timer.schedule(setMuteTask, INITIAL_MUTE_DELAY, MUTE_DELAY)
            }
        }

        fun stop() {
            if (disposed) return

            synchronized(this) {
                setMuteTask?.cancel()
                setMuteTask = null
            }
        }

        fun dispose() {
            stop()
            disposed = true
        }
    }
}

private const val INITIAL_MUTE_DELAY: Long = 3000
private const val MUTE_DELAY: Long = 1500
