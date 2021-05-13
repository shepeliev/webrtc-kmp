package com.shepeliev.webrtckmp

import android.Manifest
import android.content.pm.PackageManager
import android.os.ParcelFileDescriptor
import androidx.core.content.ContextCompat
import com.shepeliev.webrtckmp.android.CameraPermissionException
import com.shepeliev.webrtckmp.android.RecordAudioPermissionException
import java.io.File
import org.webrtc.PeerConnectionFactory as NativePeerConnectionFactory

internal actual class PeerConnectionFactory(val native: NativePeerConnectionFactory) {

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
        val context = WebRtcKmp.applicationContext
        val result = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (result == PackageManager.PERMISSION_DENIED) throw CameraPermissionException()

        return VideoSource(native.createVideoSource(isScreencast, alignTimestamps))
    }

    actual fun createVideoTrack(id: String, videoSource: VideoSource): VideoTrack {
        return VideoTrack(native.createVideoTrack(id, videoSource.native))
    }

    actual fun createAudioSource(constraints: MediaConstraints): AudioSource {
        val context = WebRtcKmp.applicationContext
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
}
