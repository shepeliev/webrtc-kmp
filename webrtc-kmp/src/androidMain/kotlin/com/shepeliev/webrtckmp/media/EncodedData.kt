package com.shepeliev.webrtckmp.media

import android.media.MediaCodec
import android.media.MediaFormat
import java.nio.ByteBuffer

data class EncodedData(val buffer: ByteBuffer, val bufferInfo: MediaCodec.BufferInfo, val mediaFormat: MediaFormat)
