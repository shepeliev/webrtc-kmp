package com.shepeliev.webrtckmm

import android.content.Context
import com.shepeliev.webrtckmm.utils.toNative
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.PeerConnectionFactory.InitializationOptions
import org.webrtc.audio.AudioDeviceModule
import org.webrtc.PeerConnectionFactory as AndroidPeerConnectionFactory

actual class PeerConnectionFactory private constructor(val native: AndroidPeerConnectionFactory) {
    actual companion object {
        actual fun initialize(
            context: Any?,
            fieldTrials: String,
            enableInternalTracer: Boolean
        ) {
            require(context is Context) { "context' must be an instance of android.content.Context" }

            val options = InitializationOptions.builder(context)
                .setFieldTrials(fieldTrials)
                .setEnableInternalTracer(enableInternalTracer)
                .createInitializationOptions()
            AndroidPeerConnectionFactory.initialize(options)

        }

        actual fun build(
            options: Options?,
            eglContext: Any?,
            audioDeviceModule: Any?
        ): PeerConnectionFactory {
            require(eglContext is EglBase) { "eglContext must be instance of EglBase" }
            if (audioDeviceModule != null) {
                require(audioDeviceModule is AudioDeviceModule) {
                    "audioDeviceModule must be AudioDeviceModule instance"
                }
            }

            val nativeOptions = options?.let {
                AndroidPeerConnectionFactory.Options().apply {
                    networkIgnoreMask = it.networkIgnoreMask
                    disableEncryption = it.disableEncryption
                    disableNetworkMonitor = it.disableNetworkMonitor
                }
            }

            val builder = AndroidPeerConnectionFactory.builder().apply {
                setOptions(nativeOptions)
                setVideoEncoderFactory(
                    DefaultVideoEncoderFactory(
                        eglContext.eglBaseContext,
                        false,
                        false
                    )
                )
                setVideoDecoderFactory(DefaultVideoDecoderFactory(eglContext.eglBaseContext))
                audioDeviceModule?.let { setAudioDeviceModule(audioDeviceModule as AudioDeviceModule) }
            }

            return PeerConnectionFactory(builder.createPeerConnectionFactory())
        }
    }

    actual fun createPeerConnection(
        rtcConfig: RtcConfiguration,
        observer: PeerConnectionObserver
    ): PeerConnection? {
        val nativeRtcConfig = rtcConfig.toNative()
        return native.createPeerConnection(
            nativeRtcConfig,
            CommonPeerConnectionObserverAdapter(observer)
        )?.let { PeerConnection(it) }
    }

    actual fun createLocalMediaStream(label: String): MediaStream {
        return MediaStream(native.createLocalMediaStream(label))
    }

    actual fun createVideoSource(
        isScreencast: Boolean,
        alignTimestamps: Boolean
    ): VideoSource {
        return VideoSource(native.createVideoSource(isScreencast, alignTimestamps))
    }

    actual fun createVideoTrack(id: String, videoSource: VideoSource): VideoTrack {
        return VideoTrack(native.createVideoTrack(id, videoSource.native))
    }

    actual fun createAudioSource(constraints: MediaConstraints): AudioSource {
        return AudioSource(native.createAudioSource(constraints.toNative()))
    }

    actual fun createAudioTrack(id: String, audioSource: AudioSource): AudioTrack {
        return AudioTrack(native.createAudioTrack(id, audioSource.native))
    }

    actual fun startAecDump(fileDescriptor: Int, fileSizeLimitBytes: Int) {
        native.startAecDump(fileDescriptor, fileSizeLimitBytes)
    }

    actual fun stopAecDump() = native.stopAecDump()
    actual fun dispose() = native.dispose()
}
