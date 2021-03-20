package com.shepeliev.webrtckmm

import android.os.ParcelFileDescriptor
import com.shepeliev.webrtckmm.android.ApplicationContextProvider
import com.shepeliev.webrtckmm.android.AudioDeviceModuleProvider
import com.shepeliev.webrtckmm.android.VideoDecoderFactoryProvider
import com.shepeliev.webrtckmm.android.VideoEncoderFactoryProvider
import java.io.File
import org.webrtc.PeerConnectionFactory as AndroidPeerConnectionFactory

actual class PeerConnectionFactory private constructor(val native: AndroidPeerConnectionFactory) {
    actual companion object {
        actual fun build(options: Options?): PeerConnectionFactory {
            val context = ApplicationContextProvider.applicationContext
            val fieldTrials = context.getString(R.string.webRtcKmm_fieldTrials)
            val enableInternalTracer = context.getString(R.string.webRtcKmm_fieldTrials).toBoolean()
            val initOptions = AndroidPeerConnectionFactory.InitializationOptions.builder(context)
                .setFieldTrials(fieldTrials)
                .setEnableInternalTracer(enableInternalTracer)
                .createInitializationOptions()
            AndroidPeerConnectionFactory.initialize(initOptions)

            val nativeOptions = options?.let {
                AndroidPeerConnectionFactory.Options().apply {
                    var ignoreMask = 0
                    if (it.ignoreEthernetNetworkAdapter) ignoreMask = ignoreMask or 1
                    if (it.ignoreWiFiNetworkAdapter) ignoreMask = ignoreMask or 2
                    if (it.ignoreCellularNetworkAdapter) ignoreMask = ignoreMask or 4
                    if (it.ignoreVpnNetworkAdapter) ignoreMask = ignoreMask or 8
                    if (it.ignoreLoopbackNetworkAdapter) ignoreMask = ignoreMask or 16
                    if (ignoreMask == 31) ignoreMask = ignoreMask or 32
                    networkIgnoreMask = ignoreMask
                    disableEncryption = it.disableEncryption
                    disableNetworkMonitor = it.disableNetworkMonitor
                }
            }

            val builder = AndroidPeerConnectionFactory.builder()
                .setOptions(nativeOptions)
                .setVideoEncoderFactory(VideoEncoderFactoryProvider.getVideoEncoderFactory())
                .setVideoDecoderFactory(VideoDecoderFactoryProvider.getVideoDecoderFactory())
                .setAudioDeviceModule(AudioDeviceModuleProvider.getAudioDeviceModule())

            return PeerConnectionFactory(builder.createPeerConnectionFactory())
        }
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
        return AudioSource(native.createAudioSource(constraints.native))
    }

    actual fun createAudioTrack(id: String, audioSource: AudioSource): AudioTrack {
        return AudioTrack(native.createAudioTrack(id, audioSource.native))
    }

    actual fun startAecDump(filePath: String, fileSizeLimitBytes: Int) {
        val fileDescriptor = ParcelFileDescriptor.open(
            File(filePath),
            ParcelFileDescriptor.MODE_READ_WRITE or
                    ParcelFileDescriptor.MODE_CREATE or
                    ParcelFileDescriptor.MODE_TRUNCATE
        )
        native.startAecDump(fileDescriptor.detachFd(), fileSizeLimitBytes)
    }

    actual fun stopAecDump() = native.stopAecDump()
    actual fun dispose() = native.dispose()
}
