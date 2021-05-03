package com.shepeliev.webrtckmp.android

import org.webrtc.EglBase

fun interface EglBaseProvider  {
    fun getEglBase(): EglBase

    companion object : EglBaseProvider {
        private val defaultEglBase by lazy { EglBase.create() }
        private var provider: EglBaseProvider = EglBaseProvider { defaultEglBase }

        fun override(provider: EglBaseProvider) {
            this.provider = provider
        }

        override fun getEglBase(): EglBase = provider.getEglBase()
    }
}
