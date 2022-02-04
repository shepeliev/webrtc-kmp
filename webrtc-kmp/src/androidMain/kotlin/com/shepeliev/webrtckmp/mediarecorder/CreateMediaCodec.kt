package com.shepeliev.webrtckmp.mediarecorder

import android.media.MediaCodec
import android.media.MediaCodecList
import android.media.MediaFormat

internal fun createMediaCodec(mediaFormat: MediaFormat, callback: MediaCodec.Callback): MediaCodec {
    return MediaCodec.createByCodecName(getCodecName(mediaFormat)).apply {
        setCallback(callback)
        configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
    }
}

private fun getCodecName(mediaFormat: MediaFormat): String =
    MediaCodecList(MediaCodecList.REGULAR_CODECS).findEncoderForFormat(mediaFormat)
        ?: error("No codec for media format: $mediaFormat")
