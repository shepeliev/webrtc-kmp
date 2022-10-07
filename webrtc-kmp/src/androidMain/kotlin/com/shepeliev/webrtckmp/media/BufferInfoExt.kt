package com.shepeliev.webrtckmp.media

import android.media.MediaCodec

internal val MediaCodec.BufferInfo.isEndOfStream get() = flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0

internal val MediaCodec.BufferInfo.isCodecConfig get() = flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0
