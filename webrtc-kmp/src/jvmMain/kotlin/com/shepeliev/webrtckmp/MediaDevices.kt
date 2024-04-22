@file:JvmName("JVMMediaDevices")

package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.media.Device
import dev.onvoid.webrtc.media.DeviceChangeListener
import dev.onvoid.webrtc.media.audio.AudioDevice
import dev.onvoid.webrtc.media.audio.AudioOptions
import dev.onvoid.webrtc.media.video.VideoCaptureCapability
import dev.onvoid.webrtc.media.video.VideoDesktopSource
import dev.onvoid.webrtc.media.video.VideoDevice
import dev.onvoid.webrtc.media.video.VideoDeviceSource
import java.util.UUID
import dev.onvoid.webrtc.media.MediaDevices as NativeMediaDevices

internal actual val mediaDevices: MediaDevices = MediaDevicesImpl

interface MediaDeviceListener {
    fun deviceConnected(device: MediaDeviceInfo)
    fun deviceDisconnected(device: MediaDeviceInfo)
}

internal object MediaDevicesImpl : MediaDevices, DeviceChangeListener {

    private val deviceListeners: MutableList<MediaDeviceListener> = mutableListOf()

    init {
        NativeMediaDevices.addDeviceChangeListener(this)
    }

    override suspend fun getUserMedia(streamConstraints: MediaStreamConstraintsBuilder.() -> Unit): MediaStream {
        val constraints = MediaStreamConstraintsBuilder().let {
            streamConstraints(it)
            it.constraints
        }

        var audioTrack: AudioStreamTrack? = null
        if (constraints.audio != null) {
            val audioDevices = NativeMediaDevices.getAudioCaptureDevices()

            if (audioDevices.isNotEmpty()) {
                val device = constraints.audio.deviceId?.let { deviceId ->
                    audioDevices.first { device ->
                        device.descriptor == deviceId
                    }
                } ?: NativeMediaDevices.getDefaultAudioCaptureDevice()

                WebRtc.audioDeviceModule.setRecordingDevice(device)
                WebRtc.audioDeviceModule.initRecording()

                val mediaConstraints = AudioOptions().apply {
                    this.autoGainControl = constraints.audio.autoGainControl?.value == true
                    this.echoCancellation = constraints.audio.echoCancellation?.value == true
                    this.noiseSuppression = constraints.audio.noiseSuppression?.value == true
                }
                val audioSource = WebRtc.peerConnectionFactory.createAudioSource(mediaConstraints)
                val nativeTrack = WebRtc.peerConnectionFactory.createAudioTrack(
                    UUID.randomUUID().toString(),
                    audioSource
                )
                audioTrack = LocalAudioStreamTrack(nativeTrack, audioSource, constraints.audio)
            }
        }

        var videoTrack: LocalVideoStreamTrack? = null
        if (constraints.video != null) {
            videoTrack = getLocalVideoStreamTrack(constraints.video)
        }

        return MediaStream().apply {
            if (audioTrack != null) addTrack(audioTrack)
            if (videoTrack != null) addTrack(videoTrack)
        }
    }

    private fun getLocalVideoStreamTrack(constraints: MediaTrackConstraints): LocalVideoStreamTrack? {
        val videoDevicesWithCapabilities = NativeMediaDevices.getVideoCaptureDevices().map {
            Pair(it, getMatchingCapabilities(it, constraints))
        }

        if (videoDevicesWithCapabilities.isNotEmpty()) {
            val matchingDevice = constraints.deviceId?.let { deviceId ->
                videoDevicesWithCapabilities.first { device ->
                    device.first.descriptor == deviceId
                }
            } ?: videoDevicesWithCapabilities.firstOrNull {
                it.second.isNotEmpty()
            }

            if (matchingDevice != null) {
                val videoSource = VideoDeviceSource().apply {
                    setVideoCaptureDevice(matchingDevice.first)
                    setVideoCaptureCapability(matchingDevice.second.first())
                }
                val nativeTrack = WebRtc.peerConnectionFactory.createVideoTrack(
                    UUID.randomUUID().toString(),
                    videoSource,
                )
                return LocalVideoStreamTrack(
                    native = nativeTrack,
                    videoSource = videoSource,
                    settings = MediaTrackSettings(),
                )
            }
        }

        return null
    }

    internal fun getMatchingCapabilities(device: VideoDevice, constraints: MediaTrackConstraints): List<VideoCaptureCapability> {
        val capabilities = NativeMediaDevices.getVideoCaptureCapabilities(device)

        val exact = capabilities.firstOrNull { capability ->
            val matchHeight = constraints.height?.exact?.let { it == capability.height } ?: true
            val matchWidth = constraints.width?.exact?.let { it == capability.width } ?: true
            val matchFrameRate = constraints.frameRate?.exact?.let { it.toInt() == capability.frameRate } ?: true

            matchHeight && matchWidth && matchFrameRate
        }?.let {
            listOf(it)
        }

        return exact ?: capabilities.filter { capability ->
            val satisfyHeight = constraints.height?.value?.let {
                it >= capability.height
            } ?: true

            val satisfyWidth = constraints.width?.value?.let {
                it >= capability.width
            } ?: true

            val satisfyFrameRate = constraints.frameRate?.value?.let {
                it >= capability.frameRate
            } ?: true

            satisfyHeight && satisfyWidth && satisfyFrameRate
        }
    }

    override suspend fun getDisplayMedia(): MediaStream {
        val source = VideoDesktopSource()
        val track = WebRtc.peerConnectionFactory.createVideoTrack("desktop", source)

        return MediaStream().apply {
            addTrack(
                DesktopVideoStreamTrack(
                    native = track,
                    videoSource = source,
                    settings = MediaTrackSettings(),
                )
            )
        }
    }

    override suspend fun supportsDisplayMedia(): Boolean = true

    override suspend fun enumerateDevices(): List<MediaDeviceInfo> {
        val audioInputDevices = NativeMediaDevices.getAudioCaptureDevices().map {
            MediaDeviceInfo(
                deviceId = it.descriptor,
                label = it.name,
                kind = MediaDeviceKind.AudioInput
            )
        }
        val audioOutputDevices = NativeMediaDevices.getAudioRenderDevices().map {
            MediaDeviceInfo(
                deviceId = it.descriptor,
                label = it.name,
                kind = MediaDeviceKind.AudioOutput
            )
        }
        val videoDevices = NativeMediaDevices.getVideoCaptureDevices().map {
            MediaDeviceInfo(
                deviceId = it.descriptor,
                label = it.name,
                kind = MediaDeviceKind.VideoInput
            )
        }

        return audioInputDevices + audioOutputDevices + videoDevices
    }

    fun addDeviceChangeListener(listener: MediaDeviceListener) {
        deviceListeners.add(listener)
    }

    fun removeDeviceChangeListener(listener: MediaDeviceListener) {
        deviceListeners.remove(listener)
    }

    override fun deviceConnected(device: Device) {
        val deviceInfo = MediaDeviceInfo(
            deviceId = device.descriptor,
            label = device.name,
            kind = when (device) {
                is AudioDevice -> MediaDeviceKind.AudioInput
                else -> MediaDeviceKind.VideoInput
            }
        )
        deviceListeners.forEach {
            it.deviceConnected(deviceInfo)
        }
    }

    override fun deviceDisconnected(device: Device) {
        val deviceInfo = MediaDeviceInfo(
            deviceId = device.descriptor,
            label = device.name,
            kind = when (device) {
                is AudioDevice -> if (NativeMediaDevices.getAudioCaptureDevices().contains(device))
                    MediaDeviceKind.AudioInput
                else
                    MediaDeviceKind.AudioOutput

                else -> MediaDeviceKind.VideoInput
            }
        )
        deviceListeners.forEach {
            it.deviceDisconnected(deviceInfo)
        }
    }
}
