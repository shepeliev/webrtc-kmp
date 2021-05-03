package com.shepeliev.webrtckmp

import WebRTC.RTCI420BufferProtocol
import WebRTC.RTCVideoFrame
import WebRTC.RTCVideoFrameBufferProtocol

actual class VideoFrame internal constructor(val native: RTCVideoFrame) {
    actual val buffer: VideoFrameBuffer
        get() = VideoFrameBufferImpl(native.buffer)

    actual val rotation: Int
        get() = native.rotation.toInt()

    actual val timestampNs: Long
        get() = native.timeStampNs

    actual val rotatedWidth: Int
        get() = native.width

    actual val rotatedHeight: Int
        get() = native.height

    actual fun retain() {
        // not applicable
    }

    actual fun release() {
        // not applicable
    }
}

private open class VideoFrameBufferImpl(val native: RTCVideoFrameBufferProtocol) : VideoFrameBuffer {
    override val width: Int
        get() = native.width

    override val height: Int
        get() = native.width

    override val i420: I420Buffer
        get() = I420BufferImpl(native.toI420())

    override fun retain() {
        // not applicable
    }

    override fun realease() {
        // not applicable
    }

    override fun cropAndScale(
        cropX: Int,
        cropY: Int,
        cropWidth: Int,
        cropHeight: Int,
        scaleWidth: Int,
        scaleHeight: Int
    ): VideoFrameBuffer {
        return this
    }
}

private class I420BufferImpl(native: RTCI420BufferProtocol) :
    VideoFrameBufferImpl(native), I420Buffer {

    val nativeI420Buffer: RTCI420BufferProtocol = native

    // TODO rework it using buffers
    override val dataY: ByteArray
        get() { TODO() }

    // TODO rework it using buffers
    override val dataU: ByteArray
        get() { TODO() }

    // TODO rework it using buffers
    override val dataV: ByteArray
        get() { TODO() }

    override val strideY: Int
        get() = nativeI420Buffer.strideY

    override val strideU: Int
        get() = nativeI420Buffer.strideU

    override val strideV: Int
        get() = nativeI420Buffer.strideV
    }
