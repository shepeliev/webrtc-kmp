package com.shepeliev.webrtckmp

import android.Manifest
import android.content.pm.PackageManager
import android.os.ParcelFileDescriptor
import androidx.core.content.ContextCompat
import com.shepeliev.webrtckmp.android.ApplicationContextProvider
import com.shepeliev.webrtckmp.android.AudioDeviceModuleProvider
import com.shepeliev.webrtckmp.android.CameraPermissionException
import com.shepeliev.webrtckmp.android.RecordAudioPermissionException
import com.shepeliev.webrtckmp.android.VideoDecoderFactoryProvider
import com.shepeliev.webrtckmp.android.VideoEncoderFactoryProvider
import java.io.File
import org.webrtc.PeerConnectionFactory as NativePeerConnectionFactory

internal actual class PeerConnectionFactory private constructor(
    val native: NativePeerConnectionFactory
) {

    actual companion object {
        actual fun build(options: Options?): PeerConnectionFactory {
            val nativeOptions = options?.let {
                NativePeerConnectionFactory.Options().apply {
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

            val builder = NativePeerConnectionFactory.builder()
                .setOptions(nativeOptions)
                .setVideoEncoderFactory(VideoEncoderFactoryProvider.getVideoEncoderFactory())
                .setVideoDecoderFactory(VideoDecoderFactoryProvider.getVideoDecoderFactory())
                .setAudioDeviceModule(AudioDeviceModuleProvider.getAudioDeviceModule())

            return PeerConnectionFactory(builder.createPeerConnectionFactory())
        }
    }

    actual fun createPeerConnection(
        rtcConfiguration: RtcConfiguration,
        constraints: MediaConstraints,
    ): PeerConnection {
        val factory = native

        return PeerConnection().apply {
            native = factory.createPeerConnection(rtcConfiguration.native, pcObserver)
                ?: error("Creating PeerConnection failed")
        }
    }

    actual fun createLocalMediaStream(id: String): MediaStream {
        return MediaStream(native.createLocalMediaStream(id))
    }

    actual fun createVideoSource(
        isScreencast: Boolean,
        alignTimestamps: Boolean
    ): VideoSource {
        val context = ApplicationContextProvider.applicationContext
        val result = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (result == PackageManager.PERMISSION_DENIED) throw CameraPermissionException()

        return VideoSource(native.createVideoSource(isScreencast, alignTimestamps))
    }

    actual fun createVideoTrack(id: String, videoSource: VideoSource): VideoTrack {
        return VideoTrack(native.createVideoTrack(id, videoSource.native))
    }

    actual fun createAudioSource(constraints: MediaConstraints): AudioSource {
        val context = ApplicationContextProvider.applicationContext
        val result = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
        if (result == PackageManager.PERMISSION_DENIED) throw RecordAudioPermissionException()

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

    actual fun dispose() {
        peerConnectionFactory.native.dispose()
    }
}
