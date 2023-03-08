package com.shepeliev.webrtckmp

import WebRTC.CGSize
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

actual class VideoStreamTrack internal constructor(
    ios: RTCVideoTrack,
    private val videoCaptureController: VideoCaptureController? = null,
) : MediaStreamTrack(ios) {

    // Setup track mute detector for remote tracks only.
    private val trackMuteDetector =
        if (videoCaptureController == null) TrackMuteDetector() else null

    init {
        videoCaptureController?.startCapture()
        trackMuteDetector?.let {
            addRenderer(it)
            it.start()
        }
    }

    fun addRenderer(renderer: RTCVideoRendererProtocol) {
        (ios as RTCVideoTrack).addRenderer(renderer)
    }

    fun removeRenderer(renderer: RTCVideoRendererProtocol) {
        (ios as RTCVideoTrack).removeRenderer(renderer)
    }

    actual suspend fun switchCamera(deviceId: String?) {
        (videoCaptureController as? CameraVideoCaptureController)?.let { controller ->
            deviceId?.let { controller.switchCamera(deviceId) } ?: controller.switchCamera()
        }
    }

    override fun onSetEnabled(enabled: Boolean) {
        if (enabled) {
            videoCaptureController?.startCapture()
        } else {
            videoCaptureController?.stopCapture()
        }
    }

    override fun onStop() {
        videoCaptureController?.stopCapture()
        trackMuteDetector?.let {
            removeRenderer(it)
            it.dispose()
        }
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

            timer = dispatch_source_create(
                type = DISPATCH_SOURCE_TYPE_TIMER,
                handle = 0,
                mask = 0,
                queue = dispatch_get_main_queue(),
            )
            dispatch_source_set_timer(
                source = timer,
                start = dispatch_time(
                    DISPATCH_TIME_NOW,
                    (INITIAL_MUTE_DELAY * NSEC_PER_SEC.toDouble()).toLong(),
                ),
                interval = (MUTE_DELAY * NSEC_PER_SEC.toDouble()).toULong(),
                leeway = NSEC_PER_SEC / 10.toULong(),
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
