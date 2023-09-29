package com.shepeliev.webrtckmp

import WebRTC.RTCLogEx
import WebRTC.RTCLoggingSeverity
import WebRTC.RTCVideoFrame
import WebRTC.RTCVideoRendererProtocol
import WebRTC.RTCVideoTrack
import kotlinx.cinterop.CValue
import platform.CoreGraphics.CGSize
import platform.Foundation.NSTimeInterval
import platform.darwin.DISPATCH_SOURCE_TYPE_TIMER
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.NSEC_PER_SEC
import platform.darwin.NSObject
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_resume
import platform.darwin.dispatch_source_cancel
import platform.darwin.dispatch_source_create
import platform.darwin.dispatch_source_set_event_handler
import platform.darwin.dispatch_source_set_timer
import platform.darwin.dispatch_source_t
import platform.darwin.dispatch_time
import kotlin.native.concurrent.AtomicInt

internal class RemoteVideoTrack internal constructor(
    ios: RTCVideoTrack,
) : RenderedVideoTrack(ios), VideoTrack {
    override var shouldReceive: Boolean?
        get() = (native as RTCVideoTrack).shouldReceive
        set(value) { (native as RTCVideoTrack).shouldReceive = checkNotNull(value) }

    private val trackMuteDetector = TrackMuteDetector().apply {
        addRenderer(this)
        start()
    }

    override suspend fun switchCamera(deviceId: String?) {
        RTCLogEx(
            RTCLoggingSeverity.RTCLoggingSeverityWarning,
            "switchCamera is not supported for remote tracks"
        )
    }

    override fun onSetEnabled(enabled: Boolean) {
        if (enabled) {
            trackMuteDetector.start()
        } else {
            trackMuteDetector.stop()
        }
    }

    override fun onStop() {
        removeRenderer(trackMuteDetector)
        trackMuteDetector.dispose()
    }

    /**
     * Implements 'mute'/'unmute' events for remote video tracks through the [RTCVideoRendererProtocol] interface.
     *
     * The original idea is from React Native WebRTC
     * https://github.com/react-native-webrtc/react-native-webrtc/blob/95cf638dfa/ios/RCTWebRTC/WebRTCModule%2BVideoTrackAdapter.m
     */
    private inner class TrackMuteDetector : NSObject(), RTCVideoRendererProtocol {
        private var timer: dispatch_source_t = null
        private var frameCount: AtomicInt = AtomicInt(0)
        private var disposed = false
        private var muted = false

        override fun renderFrame(frame: RTCVideoFrame?) {
            frameCount.increment()
        }

        override fun setSize(size: CValue<CGSize>) {
            // do nothing
        }

        fun start() {
            if (disposed) return

            if (timer != null) {
                dispatch_source_cancel(timer)
            }

            timer =
                dispatch_source_create(DISPATCH_SOURCE_TYPE_TIMER, 0u, 0u, dispatch_get_main_queue())
            dispatch_source_set_timer(
                timer,
                dispatch_time(
                    DISPATCH_TIME_NOW,
                    (INITIAL_MUTE_DELAY * NSEC_PER_SEC.toDouble()).toLong()
                ),
                (MUTE_DELAY * NSEC_PER_SEC.toDouble()).toULong(),
                NSEC_PER_SEC / 10.toULong()
            )

            var lastFrameCount = frameCount.value
            dispatch_source_set_event_handler(timer) {
                if (disposed) {
                    return@dispatch_source_set_event_handler
                }

                val muted = lastFrameCount == frameCount.value
                if (this.muted != muted) {
                    this.muted = muted
                    setMute(muted)
                }

                lastFrameCount = frameCount.value
            }

            dispatch_resume(timer)
        }

        fun stop() {
            if (disposed) return

            if (timer != null) {
                dispatch_source_cancel(timer)
                timer = null
            }
        }

        fun dispose() {
            stop()
            disposed = true
        }
    }
}

private const val INITIAL_MUTE_DELAY: NSTimeInterval = 3.0
private const val MUTE_DELAY: NSTimeInterval = 1.5
