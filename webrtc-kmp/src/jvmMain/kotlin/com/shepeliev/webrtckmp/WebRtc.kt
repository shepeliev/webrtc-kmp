@file:JvmName("WebRtcKmpJVM")

package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.PeerConnectionFactory
import dev.onvoid.webrtc.logging.Logging
import dev.onvoid.webrtc.media.DeviceChangeListener
import dev.onvoid.webrtc.media.MediaDevices
import dev.onvoid.webrtc.media.audio.AudioDeviceModule
import dev.onvoid.webrtc.media.audio.AudioProcessing

object WebRtc {

    private var _peerConnectionFactory: PeerConnectionFactory? = null
    internal val peerConnectionFactory: PeerConnectionFactory
        get() {
            if (_peerConnectionFactory == null) initialize()
            return checkNotNull(_peerConnectionFactory)
        }

    private var _audioDeviceModule: AudioDeviceModule? = null
    internal val audioDeviceModule: AudioDeviceModule
        get() {
            if (_audioDeviceModule == null) initialize()
            return checkNotNull(_audioDeviceModule)
        }

    private val builder by lazy {
        WebRtcBuilder()
    }

    fun configureBuilder(block: WebRtcBuilder.() -> Unit = {}) {
        block(builder)
    }

    private fun initialize() {
        initLogging()
        initializePeerConnectionFactory()
    }

    private fun initializePeerConnectionFactory() {
        with(builder) {
            val audioModule = audioModule ?: AudioDeviceModule().apply {
                MediaDevices.getDefaultAudioRenderDevice()?.let {
                    setPlayoutDevice(it)
                    initPlayout()
                }
            }
            _audioDeviceModule = audioModule
            _peerConnectionFactory = PeerConnectionFactory(
                audioModule,
                audioProcessing,
            )
        }
    }

    private fun initLogging() {
        with(builder) {
            loggingSeverity?.let {
                Logging.addLogSink(it) { _, message ->
                    println(message)
                }
            }
        }
    }

    fun addDeviceChangeListener(listener: MediaDeviceListener) {
        MediaDevicesImpl.addDeviceChangeListener(listener)
    }

    fun removeDeviceChangeListener(listener: MediaDeviceListener) {
        MediaDevicesImpl.removeDeviceChangeListener(listener)
    }

    fun setAudioOutputDevice(device: MediaDeviceInfo) {
        MediaDevices.getAudioRenderDevices().firstOrNull {
            it.descriptor == device.deviceId
        }?.let {
            audioDeviceModule.setPlayoutDevice(it)
            audioDeviceModule.initPlayout()
        }
    }

    fun getDefaultAudioOutput(): MediaDeviceInfo? {
        return MediaDevices.getDefaultAudioRenderDevice()?.let {
            MediaDeviceInfo(
                deviceId = it.descriptor,
                label = it.name,
                kind = MediaDeviceKind.AudioOutput,
            )
        }
    }

    fun disposePeerConnectionFactory() {
        peerConnectionFactory.dispose()
    }
}

class WebRtcBuilder(
    var loggingSeverity: Logging.Severity? = null,
    val audioModule: AudioDeviceModule? = null,
    val audioProcessing: AudioProcessing? = null,
)