package com.shepeliev.webrtckmp.android

import org.webrtc.audio.AudioDeviceModule

fun interface AudioDeviceModuleProvider {
    fun getAudioDeviceModule(): AudioDeviceModule?

    companion object : AudioDeviceModuleProvider {
        private var provider = AudioDeviceModuleProvider { null }

        override fun getAudioDeviceModule(): AudioDeviceModule? = provider.getAudioDeviceModule()

        fun override(provider: AudioDeviceModuleProvider) {
            this.provider = provider
        }
    }
}
