package com.shepeliev.webrtckmp.mediarecorder

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.view.Surface
import com.shepeliev.webrtckmp.rootEglBase
import org.webrtc.EglBase
import org.webrtc.GlRectDrawer
import org.webrtc.VideoFrame
import org.webrtc.VideoFrameDrawer
import java.nio.ByteBuffer

internal class VideoEncoder(
    private val bitsPerSecond: Int,
    private val onMediaFormatReady: (MediaFormat) -> Unit,
    private val onVideoDataAvailable: (ByteBuffer, MediaCodec.BufferInfo) -> Unit,
    private val onStreamEnded: () -> Unit,
    private val onError: (Throwable) -> Unit,
) {

    private val drawer = GlRectDrawer()
    private var frameDrawer: VideoFrameDrawer? = null
    private var recordableEglBase: EglBase? = null
    private var codec: MediaCodec? = null
    private var inputSurface: Surface? = null

    @Volatile
    private var stopped = false

    fun encodeVideoFrame(frame: VideoFrame) {
        if (stopped) return
        try {
            if (codec == null) {
                initVideoCodec(frame.rotatedWidth, frame.rotatedHeight)
            }

            frameDrawer?.drawFrame(frame, drawer, null, 0, 0, frame.rotatedWidth, frame.rotatedHeight)
            recordableEglBase?.swapBuffers()
        } finally {
            frame.release()
        }
    }

    fun stop() {
        if (stopped) return
        stopped = true
        runCatching { codec?.signalEndOfInputStream() }.onFailure(onError)
    }

    private fun initVideoCodec(outputWidth: Int, outputHeight: Int) {
        runCatching {
            val mediaFormat = createVideoFormat(outputWidth, outputHeight)
            frameDrawer = VideoFrameDrawer()
            codec = createMediaCodec(mediaFormat, MediaCodecCallback()).apply {
                inputSurface = createInputSurface()
                recordableEglBase = EglBase.create(rootEglBase.eglBaseContext, EglBase.CONFIG_RECORDABLE).apply {
                    createSurface(inputSurface)
                    makeCurrent()
                }
                start()
            }
        }.onFailure { error ->
            stopped = true
            onError(error)
        }
    }

    private fun createVideoFormat(outputWidth: Int, outputHeight: Int): MediaFormat {
        return MediaFormat.createVideoFormat(
            MediaFormat.MIMETYPE_VIDEO_AVC,
            outputWidth,
            outputHeight
        ).apply {
            setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
            )
            setInteger(MediaFormat.KEY_BIT_RATE, bitsPerSecond)
            setInteger(MediaFormat.KEY_FRAME_RATE, OUTPUT_FRAME_RATE)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL)
        }
    }

    fun dispose() {
        codec?.apply {
            stop()
            release()
        }
        codec = null

        inputSurface?.release()
        inputSurface = null

        recordableEglBase?.release()
        recordableEglBase = null

        frameDrawer?.release()
        frameDrawer = null
    }

    private inner class MediaCodecCallback : MediaCodec.Callback() {
        override fun onOutputBufferAvailable(
            codec: MediaCodec,
            index: Int,
            info: MediaCodec.BufferInfo
        ) {
            when {
                info.isEndOfStream -> onStreamEnded()

                !info.isCodecConfig -> {
                    runCatching {
                        codec.getOutputBuffer(index)?.let { onVideoDataAvailable(it, info) }
                        codec.releaseOutputBuffer(index, false)
                    }.onFailure(onError)
                }
            }
        }

        override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
            // not applicable as we use InputSurface
        }

        override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
            onMediaFormatReady(codec.outputFormat)
        }

        override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
            onError(e)
            stopped = true
        }
    }
}

private const val OUTPUT_FRAME_RATE = 30
private const val I_FRAME_INTERVAL = 5
