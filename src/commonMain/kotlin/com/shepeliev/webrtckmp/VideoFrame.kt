package com.shepeliev.webrtckmp

expect class VideoFrame {
    val buffer: VideoFrameBuffer
    val rotation: Int
    val timestampNs: Long
    val rotatedWidth: Int
    val rotatedHeight: Int

    fun retain()
    fun release()
}

// TODO rework
interface I420Buffer : VideoFrameBuffer {
    val dataY: ByteArray
    val dataU: ByteArray
    val dataV: ByteArray
    val strideY: Int
    val strideU: Int
    val strideV: Int
}

interface VideoFrameBuffer {
    val width: Int
    val height: Int
    val i420: I420Buffer

    fun retain()
    fun realease()
    fun cropAndScale(
        cropX: Int,
        cropY: Int,
        cropWidth: Int,
        cropHeight: Int,
        scaleWidth: Int,
        scaleHeight: Int
    ): VideoFrameBuffer
}
