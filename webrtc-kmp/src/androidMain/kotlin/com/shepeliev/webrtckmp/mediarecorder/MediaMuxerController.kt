package com.shepeliev.webrtckmp.mediarecorder

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Environment
import com.shepeliev.webrtckmp.applicationContext
import org.webrtc.Logging
import java.io.File
import java.nio.ByteBuffer

internal class MediaMuxerController(mimeType: MimeType, private val onDataAvailable: (String) -> Unit) {

    private val tag = "MediaMuxerController"
    private val outputPath: String = getOutputPath(mimeType)
    private val muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

    private var started = false
    private var videoTrackId = -1
    private var audioTrackId = -1
    private var videoTrackFinished = false
    private var audioTrackFinished = true // TODO make false when audio encoder is implemented

    private fun getOutputPath(mimeType: MimeType): String {
        val isExternalStorageAvailable = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
        val cacheDir: File = if (isExternalStorageAvailable) {
            applicationContext.externalCacheDir ?: applicationContext.cacheDir
        } else {
            applicationContext.cacheDir
        }

        return File.createTempFile("media", ".${mimeType.fileExtension}", cacheDir).absolutePath
    }

    fun addVideoTrack(mediaFormat: MediaFormat) {
        videoTrackId = muxer.addTrack(mediaFormat)
        Logging.d(tag, "Video track added [id = $videoTrackId]")
        startIfRequired()
    }

    fun addAudioTrack(mediaFormat: MediaFormat) {
        audioTrackId = muxer.addTrack(mediaFormat)
        Logging.d(tag, "Audio track added [id = $videoTrackId]")
        startIfRequired()
    }

    private fun startIfRequired() {
        if (started || videoTrackId == -1) return
        muxer.start()
        started = true
        Logging.d(tag, "Media muxer started")
    }

    fun writeVideoSampleData(buffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        writeSampleData(videoTrackId, buffer, bufferInfo)
    }

    fun writeAudioSampleData(buffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        writeSampleData(audioTrackId, buffer, bufferInfo)
    }

    private fun writeSampleData(trackId: Int, buffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        if (!started) return
        Logging.d(tag, "Write sample data [trackId = $trackId, timestamp = ${bufferInfo.presentationTimeUs}]")
        muxer.writeSampleData(trackId, buffer, bufferInfo)
    }

    fun onVideoTrackFinished() {
        Logging.d(tag, "Video track finished")
        videoTrackFinished = true
        if (videoTrackFinished && audioTrackFinished) complete()
    }

    fun onAudioTrackFinished() {
        Logging.d(tag, "Audio track finished")
        audioTrackFinished = true
        if (videoTrackFinished && audioTrackFinished) complete()
    }

    private fun complete() {
        muxer.stop()
        muxer.release()
        onDataAvailable(outputPath)
        Logging.d(tag, "Media muxer stopped and released")
    }
}
