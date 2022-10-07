package com.shepeliev.webrtckmp.media

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Handler
import androidx.core.content.ContextCompat
import com.shepeliev.webrtckmp.ApplicationContextHolder

private const val AUDIO_SOURCE = android.media.MediaRecorder.AudioSource.CAMCORDER
private const val SAMPLE_RATE = 44100
private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT

internal class AudioEncoder(
    private val bitRate: Int,
    handler: Handler,
    private val onData: (EncodedData?, Throwable?) -> Unit,
) {

    private val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
    private lateinit var codec: MediaCodec
    private lateinit var audioRecord: AudioRecord

    @Volatile
    private var stopped = false
    private var started = false
    private val lock = Any()

    init {
        check(
            ContextCompat.checkSelfPermission(
                ApplicationContextHolder.context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) { "RECORD_AUDIO permission is not granted" }
        handler.post {
            synchronized(lock) {
                if (stopped) return@post
                started = true
                codec = createMediaCodec()
                audioRecord = AudioRecord(AUDIO_SOURCE, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, bufferSize)
                audioRecord.startRecording()
                codec.start()
            }
        }
    }

    fun stop() {
        synchronized(lock) {
            if (stopped || !started) return
            stopped = true
            audioRecord.stop()
        }
    }

    fun dispose() {
        stop()
        codec.stop()
        codec.release()
        audioRecord.release()
    }

    private fun createMediaCodec(): MediaCodec {
        val mediaFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, SAMPLE_RATE, 1).apply {
            setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
            setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectERLC)
        }

        return MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC).also {
            it.setCallback(MediaCodecCallback())
            it.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
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
            runCatching {
                val inputBuffer = checkNotNull(codec.getInputBuffer(index))
                if (stopped) {
                    codec.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                } else {
                    val readSize = inputBuffer.capacity().coerceAtMost(bufferSize)
                    val count = audioRecord.read(inputBuffer, readSize)
                    codec.queueInputBuffer(index, 0, count.coerceAtLeast(0), System.nanoTime() / 1000, 0)
                }
            }.onFailure { onData(null, it) }
        }

        override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
            // as we use codec.getOutputFormat() it can be ignored
        }

        override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
            onData(null, e)
            synchronized(lock) {
                stopped = true
                audioRecord.stop()
            }
        }
    }
}
