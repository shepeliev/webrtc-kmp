package com.shepeliev.webrtckmp.android

import com.shepeliev.webrtckmp.WebRtcKmp
import com.shepeliev.webrtckmp.eglBase
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.VideoEncoderFactory

fun interface VideoEncoderFactoryProvider {
    fun getVideoEncoderFactory(): VideoEncoderFactory

    companion object : VideoEncoderFactoryProvider {
        private var provider = VideoEncoderFactoryProvider {
            val eglBase = WebRtcKmp.eglBase
            DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true)
        }

        override fun getVideoEncoderFactory(): VideoEncoderFactory =
            provider.getVideoEncoderFactory()

        fun override(provider: VideoEncoderFactoryProvider) {
            this.provider = provider
        }
    }
}
