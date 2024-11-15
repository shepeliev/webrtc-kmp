@file:JvmName("WebRtcKmpJVM")

package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.PeerConnectionFactory
import dev.onvoid.webrtc.logging.Logging
import dev.onvoid.webrtc.media.MediaDevices
import dev.onvoid.webrtc.media.audio.AudioDevice
import dev.onvoid.webrtc.media.audio.AudioDeviceModule
import dev.onvoid.webrtc.media.audio.AudioProcessing
import java.util.Optional

typealias AudioDeviceModuleBuilder = () -> AudioDeviceModule?

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
            _audioDeviceModule = audioModuleBuilder()

            _peerConnectionFactory = PeerConnectionFactory(
                _audioDeviceModule,
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

    fun setAudioOutputDevice(device: AudioDevice) {
        audioDeviceModule.stopPlayout()
        audioDeviceModule.setPlayoutDevice(device)
        audioDeviceModule.initPlayout()
    }

    fun setAudioOutputDevice(device: MediaDeviceInfo) {
        MediaDevices.getAudioRenderDevices().firstOrNull {
            it.descriptor == device.deviceId
        }?.let {
            setAudioOutputDevice(it)
        }
    }

    fun setAudioInputDevice(device: AudioDevice) {
        audioDeviceModule.stopRecording()
        audioDeviceModule.setRecordingDevice(device)
        audioDeviceModule.initRecording()
    }

    fun setAudioInputDevice(device: MediaDeviceInfo) {
        MediaDevices.getAudioCaptureDevices().firstOrNull {
            it.descriptor == device.deviceId
        }?.let {
            setAudioInputDevice(it)
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

internal val defaultAudioDeviceModuleBuilder: AudioDeviceModuleBuilder = {
    AudioDeviceModule().apply {
        MediaDevices.getDefaultAudioRenderDevice()?.let {
            setPlayoutDevice(it)
            initPlayout()
            startPlayout()
        }
    }
}

class WebRtcBuilder(
    var loggingSeverity: Logging.Severity? = null,
    var audioModuleBuilder: AudioDeviceModuleBuilder = defaultAudioDeviceModuleBuilder,
    var audioProcessing: AudioProcessing? = null,
)
