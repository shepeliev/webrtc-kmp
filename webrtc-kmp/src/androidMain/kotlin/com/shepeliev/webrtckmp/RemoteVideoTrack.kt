package com.shepeliev.webrtckmp

import org.webrtc.Logging
import org.webrtc.VideoFrame
import org.webrtc.VideoSink
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.atomic.AtomicInteger
import org.webrtc.VideoTrack as AndroidVideoTrack

internal class RemoteVideoTrack(
    native: AndroidVideoTrack,
) : RenderedVideoTrack(native), VideoTrack {
    override var shouldReceive: Boolean?
        get() = (native as AndroidVideoTrack).shouldReceive()
        set(value) { (native as AndroidVideoTrack).setShouldReceive(checkNotNull(value)) }

    private val trackMuteDetector = TrackMuteDetector().apply {
        addSink(this)
        start()
    }

    override suspend fun switchCamera(deviceId: String?) {
        Logging.e("RemoteVideoStreamTrack", "switchCamera is not supported for remote tracks")
    }

    override fun onSetEnabled(enabled: Boolean) {
        if (enabled) {
            trackMuteDetector.start()
        } else {
            trackMuteDetector.stop()
        }
    }

    override fun onStop() {
        removeSink(trackMuteDetector)
        trackMuteDetector.dispose()
    }

    /**
     * Implements 'mute'/'unmute' events for remote video tracks through the [VideoSink] interface.
     *
     * The original idea is from React Native WebRTC
     * https://github.com/react-native-webrtc/react-native-webrtc/blob/95cf638dfa/android/src/main/java/com/oney/WebRTCModule/VideoTrackAdapter.java#L69
     */
    private inner class TrackMuteDetector : VideoSink {
        private val timer: Timer = Timer("VideoTrackMutedTimer")
        private var setMuteTask: TimerTask? = null

        @Volatile
        private var disposed = false
        private val frameCounter: AtomicInteger = AtomicInteger()
        private var mutedState = false

        override fun onFrame(frame: VideoFrame) {
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
