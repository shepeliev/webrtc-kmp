package com.shepeliev.webrtckmp

import com.shepeliev.webrtckmp.utils.toByteArray
import org.webrtc.VideoFrame as NativeVideoFrame

actual class VideoFrame internal constructor(val native: NativeVideoFrame) {
    actual val buffer: VideoFrameBuffer
        get() = VideoFrameBufferImpl(native.buffer)

    actual val rotation: Int
        get() = native.rotation

    actual val timestampNs: Long
        get() = native.timestampNs

    actual val rotatedWidth: Int
        get() = native.rotatedWidth

    actual val rotatedHeight: Int
        get() = native.rotatedHeight

    actual fun retain() {
        native.retain()
    }

    actual fun release() {
        native.release()
    }
}

private open class VideoFrameBufferImpl(val native: NativeVideoFrame.Buffer) : VideoFrameBuffer {
    override val width: Int
        get() = native.width

    override val height: Int
        get() = native.width

    override val i420: I420Buffer
        get() = I420BufferImpl(native.toI420())

    override fun retain() {
        native.retain()
    }

    override fun realease() {
        native.release()
    }

    override fun cropAndScale(
        cropX: Int,
        cropY: Int,
        cropWidth: Int,
        cropHeight: Int,
        scaleWidth: Int,
        scaleHeight: Int
    ): VideoFrameBuffer {
        return VideoFrameBufferImpl(
            native.cropAndScale(
                cropX,
                cropY,
                cropWidth,
                cropHeight,
                scaleWidth,
                scaleHeight
            )
        )
    }
}

private class I420BufferImpl(native: NativeVideoFrame.I420Buffer) :
    VideoFrameBufferImpl(native), I420Buffer {

    val nativeI420Buffer: NativeVideoFrame.I420Buffer = native

    // TODO rework it using buffers
    override val dataY: ByteArray
        get() = nativeI420Buffer.dataY.toByteArray()

    // TODO rework it using buffers
    override val dataU: ByteArray
        get() = nativeI420Buffer.dataU.toByteArray()

    // TODO rework it using buffers
    override val dataV: ByteArray
        get() = nativeI420Buffer.dataV.toByteArray()

    override val strideY: Int
        get() = nativeI420Buffer.strideY

    override val strideU: Int
        get() = nativeI420Buffer.strideU

    override val strideV: Int
        get() = nativeI420Buffer.strideV
    }
