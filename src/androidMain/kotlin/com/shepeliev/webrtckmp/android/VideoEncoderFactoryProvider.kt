package com.shepeliev.webrtckmp.android

import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.VideoEncoderFactory

fun interface VideoEncoderFactoryProvider {
    fun getVideoEncoderFactory(): VideoEncoderFactory

    companion object : VideoEncoderFactoryProvider {
        private var provider = VideoEncoderFactoryProvider {
            val eglBase = EglBaseProvider.getEglBase()
            DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true)
        }

        override fun getVideoEncoderFactory(): VideoEncoderFactory =
            provider.getVideoEncoderFactory()

        fun override(provider: VideoEncoderFactoryProvider) {
            this.provider = provider
        }
    }
}
