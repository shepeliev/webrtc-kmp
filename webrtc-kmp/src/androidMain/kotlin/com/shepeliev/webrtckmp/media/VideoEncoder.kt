package com.shepeliev.webrtckmp.media

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaFormat
import android.os.Handler
import android.view.Surface
import com.shepeliev.webrtckmp.VideoStreamTrack
import com.shepeliev.webrtckmp.WebRtc
import org.webrtc.EglBase
import org.webrtc.GlRectDrawer
import org.webrtc.VideoFrame
import org.webrtc.VideoFrameDrawer
import org.webrtc.VideoSink

internal class VideoEncoder(
    private val videoTrack: VideoStreamTrack?,
    private val frameRate: Int,
    private val bitsPerSecond: Int,
    private val handler: Handler,
    private val onData: (EncodedData?, Throwable?) -> Unit,
) {

    private val drawer = GlRectDrawer()
    private val frameDrawer = VideoFrameDrawer()
    private var codec: MediaCodec? = null
    private var recordableEglBase: EglBase? = null
    private var inputSurface: Surface? = null

    @Volatile
    private var stopped = false

    private val videoSink = VideoSink { frame ->
        if (stopped) return@VideoSink
        frame.retain()
        handler.post {
            runCatching {
                encodeVideoFrame(frame)
            }.onFailure {
                onData(null, it)
                stopped = true
            }
            frame.release()
        }
    }

    init {
        videoTrack?.addSink(videoSink)
    }

    fun stop() {
        if (stopped) return
        stopped = true
        codec?.signalEndOfInputStream()
    }

    fun dispose() {
        stop()
        videoTrack?.removeSink(videoSink)
        codec?.stop()
        codec?.release()
        inputSurface?.release()
        recordableEglBase?.release()
        frameDrawer.release()
    }

    private fun encodeVideoFrame(frame: VideoFrame) {
        if (codec == null) initVideoCodec(frame.rotatedWidth, frame.rotatedHeight)
        frameDrawer.drawFrame(frame, drawer, null, 0, 0, frame.rotatedWidth, frame.rotatedHeight)
        recordableEglBase?.swapBuffers()
    }

    private fun initVideoCodec(outputWidth: Int, outputHeight: Int) {
        val codec = createMediaCodec(outputWidth, outputHeight)
        inputSurface = codec.createInputSurface()
        recordableEglBase = EglBase.create(WebRtc.rootEglBase.eglBaseContext, EglBase.CONFIG_RECORDABLE).apply {
            createSurface(inputSurface)
            makeCurrent()
        }
        codec.start()
    }

    private fun createMediaCodec(outputWidth: Int, outputHeight: Int): MediaCodec {
        val mediaFormat = createVideoFormat(outputWidth, outputHeight)
        val codecName = MediaCodecList(MediaCodecList.REGULAR_CODECS).findEncoderForFormat(mediaFormat)
            ?: error("No codec for media format: $mediaFormat")

        return MediaCodec.createByCodecName(codecName).also {
            it.setCallback(MediaCodecCallback())
            it.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            codec = it
        }
    }

    private fun createVideoFormat(outputWidth: Int, outputHeight: Int): MediaFormat {
        return MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, outputWidth, outputHeight).apply {
            setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
            )
            setInteger(MediaFormat.KEY_BIT_RATE, bitsPerSecond)
            setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL)
        }
    }

    private inner class MediaCodecCallback : MediaCodec.Callback() {
        override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
            if (info.isCodecConfig) return
            runCatching {
                val buffer = checkNotNull(codec.getOutputBuffer(index))
                val outputFormat = checkNotNull(codec.getOutputFormat(index))
                onData(EncodedData(buffer, info, outputFormat), null)
                if (!info.isEndOfStream) codec.releaseOutputBuffer(index, false)
            }.onFailure { onData(null, it) }
        }

        override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
            // not applicable as we use InputSurface
        }

        override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
            // as we use codec.getOutputFormat() it can be ignored
        }

        override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
            onData(null, e)
            stopped = true
        }
    }
}

private const val I_FRAME_INTERVAL = 100
