package com.shepeliev.webrtckmm

import android.util.Log
import com.shepeliev.webrtckmm.android.ApplicationContextProvider
import com.shepeliev.webrtckmm.android.EglBaseProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.webrtc.Camera1Enumerator
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.CapturerObserver
import org.webrtc.SurfaceTextureHelper
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual object MediaDevices {
    private const val tag = "MediaDevices"

    private val context by lazy { ApplicationContextProvider.applicationContext }

    private val surfaceTextureHelper by lazy {
        val eglBase = EglBaseProvider.getEglBase()
        SurfaceTextureHelper.create(UUID.randomUUID().toString(), eglBase.eglBaseContext)
    }

    private val cameraEnumerator by lazy {
        val context = ApplicationContextProvider.applicationContext
        if (Camera2Enumerator.isSupported(context)) {
            Camera2Enumerator(context)
        } else {
            Camera1Enumerator()
        }
    }

    private val audioSourceRef = AtomicReference<AudioSource>(null)
    private val videoSourceRef = AtomicReference<VideoSource>(null)
    private val videoCapturerRef = AtomicReference<CameraVideoCapturer>(null)
    private val audioTrackCounter = AtomicInteger(0)
    private val videoTrackCounter = AtomicInteger(0)

    // TODO implement video constraints
    actual suspend fun getUserMedia(audio: Boolean, video: Boolean): UserMedia {
        val factory = peerConnectionFactory
        var audioTrack: AudioTrack? = null
        if (audio) {
            // TODO implement requesting microphone permission
            val audioSource = audioSourceRef.updateAndGet {
                it ?: factory.createAudioSource(mediaConstraints())
            }
            audioTrack = factory.createAudioTrack("${UUID.randomUUID()}", audioSource)
            audioTrackCounter.incrementAndGet()
        }

        var videoTrack: VideoTrack? = null
        if (video) {
            // TODO implement requesting camera permission
            val cameras = enumerateDevices()
            val camera = cameras.find { it.isFrontFacing } ?: cameras.firstOrNull()
            if (camera != null) {
                val videoSource = videoSourceRef.updateAndGet {
                    it ?: factory.createVideoSource(false, alignTimestamps = true)
                }
                startVideoCapture(camera.deviceId, videoSource.native.capturerObserver)
                videoTrack = factory.createVideoTrack("${UUID.randomUUID()}", videoSource)
                videoTrackCounter.incrementAndGet()
            }
        }

        return UserMedia(
            audioTracks = audioTrack?.let { listOf(it) } ?: emptyList(),
            videoTracks = videoTrack?.let { listOf(it) } ?: emptyList()
        )
    }

    private fun startVideoCapture(cameraId: String, capturerObserver: CapturerObserver) {
        videoCapturerRef.updateAndGet {
            it?.stopCapture()
            it?.dispose()
            cameraEnumerator.createCapturer(cameraId, CameraEventsHandler()).apply {
                initialize(surfaceTextureHelper, context, capturerObserver)
                startCapture(1280, 720, 30)
            }
        }
    }

    actual suspend fun enumerateDevices(): List<DeviceInfo> {
        return cameraEnumerator.deviceNames.map {
            DeviceInfo(
                deviceId = it,
                label = it,
                kind = DeviceKind.videoInput,
                cameraEnumerator.isFrontFacing(it)
            )
        }
    }

    actual suspend fun switchCamera(): SwitchCameraResult {
        val videoCapturer = videoCapturerRef.get() ?: return SwitchCameraResult(
            errorDescription = "Camera capturer has not been created yet."
        )
        return suspendCoroutine { videoCapturer.switchCamera(cameraSwitchHandler(it)) }
    }

    actual suspend fun switchCamera(cameraId: String): SwitchCameraResult {
        val videoCapturer = videoCapturerRef.get() ?: return SwitchCameraResult(
            errorDescription = "Camera capturer has not been created yet."
        )
        return suspendCoroutine { videoCapturer.switchCamera(cameraSwitchHandler(it), cameraId) }
    }

    private fun cameraSwitchHandler(continuation: Continuation<SwitchCameraResult>): CameraVideoCapturer.CameraSwitchHandler {
        return object : CameraVideoCapturer.CameraSwitchHandler {
            override fun onCameraSwitchDone(isFrontCamera: Boolean) {
                continuation.resume(SwitchCameraResult(isFrontCamera))
            }

            override fun onCameraSwitchError(errorDescription: String) {
                continuation.resume(SwitchCameraResult(errorDescription = errorDescription))
            }
        }
    }

    internal fun onAudioTrackStopped(track: MediaStreamTrack) {
        Log.d(tag, "Audio track stopped: ${track.id}")
        val count = audioTrackCounter.decrementAndGet()
        if (count == 0) {
            Log.d(tag, "There is no any active audio track. Dispose audio source.")
            audioSourceRef.getAndUpdate { null }?.also {
                GlobalScope.launch(Dispatchers.Default) { it.dispose() }
            }
        }
    }

    internal fun onVideoTrackStopped(track: MediaStreamTrack) {
        Log.d(tag, "Video track stopped: ${track.id}")
        val count = videoTrackCounter.decrementAndGet()
        if (count == 0) {
            Log.d(
                tag,
                "There is no any active video track. Stop camera capture and dispose video source."
            )
            videoCapturerRef.getAndUpdate { null }?.also {
                GlobalScope.launch(Dispatchers.Default) {
                    it.stopCapture()
                    it.dispose()
                }
            }
            videoSourceRef.getAndUpdate { null }?.also {
                GlobalScope.launch(Dispatchers.Default) { it.dispose() }
            }
        }
    }

    private class CameraEventsHandler : CameraVideoCapturer.CameraEventsHandler {
        override fun onCameraError(errorDescription: String) {
            Log.e(tag, "Camera error: $errorDescription")
        }

        override fun onCameraDisconnected() {
            Log.w(tag, "Camera disconnected")
        }

        override fun onCameraFreezed(errorDescription: String) {
            Log.e(tag, "Camera freezed: $errorDescription")
        }

        override fun onCameraOpening(cameraId: String) {
            Log.d(tag, "Opening camera $cameraId")
        }

        override fun onFirstFrameAvailable() {
            Log.d(tag, "First frame available")
        }

        override fun onCameraClosed() {
            Log.d(tag, "Camera closed")
        }
    }
}