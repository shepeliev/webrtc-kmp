package com.shepeliev.webrtckmm

import android.util.Log
import com.shepeliev.webrtckmm.android.ApplicationContextProvider
import com.shepeliev.webrtckmm.android.EglBaseProvider
import com.shepeliev.webrtckmm.utils.uuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.webrtc.Camera1Enumerator
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.SurfaceTextureHelper
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
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

    private val audioTracks = ConcurrentHashMap<String, Unit>()
    private val videoTracks = ConcurrentHashMap<String, Unit>()

    private var isFrontCamera = false

    // TODO implement video constraints
    actual suspend fun getUserMedia(audio: Boolean, video: Boolean): MediaStream {
        val factory = peerConnectionFactory
        var audioTrack: AudioTrack? = null
        if (audio) {
            // TODO implement requesting microphone permission
            val audioSource = audioSourceRef.updateAndGet {
                it ?: factory.createAudioSource(mediaConstraints())
            }
            audioTrack = factory.createAudioTrack(uuid(), audioSource)
            audioTracks += audioTrack.id to Unit
        }

        var videoTrack: VideoTrack? = null
        if (video) {
            // TODO implement requesting camera permission
            isFrontCamera = false
            val cameras = enumerateDevices()
            val camera = cameras.find { it.isFrontFacing == isFrontCamera }
                ?: cameras.firstOrNull()
                ?: throw CameraVideoCapturerException("Camera not found.")

            val videoSource = videoSourceRef.updateAndGet {
                it ?: factory.createVideoSource(
                    isScreencast = false,
                    alignTimestamps = true
                )
            }
            startVideoCapture(camera.deviceId)
            videoTrack = factory.createVideoTrack(uuid(), videoSource)
            videoTracks += videoTrack.id to Unit
        }

        val nativeStream = factory.native.createLocalMediaStream(uuid())
        return MediaStream(nativeStream).apply {
            audioTrack?.let { addTrack(it) }
            videoTrack?.let { addTrack(it) }
        }
    }

    private fun startVideoCapture(cameraId: String) {
        val videoSource = videoSourceRef.get() ?: return
        videoCapturerRef.updateAndGet {
            it?.stopCapture()
            it?.dispose()
            cameraEnumerator.createCapturer(cameraId, CameraEventsHandler()).apply {
                initialize(surfaceTextureHelper, context, videoSource.nativeCapturerObserver)
                startCapture(1280, 720, 30)
            }
        }
    }

    actual suspend fun enumerateDevices(): List<MediaDeviceInfo> {
        return cameraEnumerator.deviceNames.map {
            MediaDeviceInfo(
                deviceId = it,
                label = it,
                kind = MediaDeviceKind.VideoInput,
                cameraEnumerator.isFrontFacing(it)
            )
        }
    }

    actual suspend fun switchCamera(): MediaDeviceInfo {
        val devices = enumerateDevices()
        val device = devices.firstOrNull { it.isFrontFacing == !isFrontCamera }
            ?: devices.firstOrNull()
            ?: error("Camera not found")

        switchCamera(device)
        return device
    }

    actual suspend fun switchCamera(cameraId: String): MediaDeviceInfo {
        val devices = enumerateDevices()
        val device = devices.firstOrNull { it.deviceId == cameraId }
            ?: devices.firstOrNull()
            ?: error("Camera not found")

        switchCamera(device)
        return device
    }

    private suspend fun switchCamera(camera: MediaDeviceInfo) {
        val videoCapturer = videoCapturerRef.get()
            ?: throw CameraVideoCapturerException("Camera video capturer stopped")

        suspendCoroutine<Unit> {
            videoCapturer.switchCamera(cameraSwitchHandler(it), camera.deviceId)
        }
    }

    private fun cameraSwitchHandler(continuation: Continuation<Unit>): CameraVideoCapturer.CameraSwitchHandler {
        return object : CameraVideoCapturer.CameraSwitchHandler {
            override fun onCameraSwitchDone(isFrontCamera: Boolean) {
                this@MediaDevices.isFrontCamera = isFrontCamera
                continuation.resume(Unit)
            }

            override fun onCameraSwitchError(errorDescription: String) {
                continuation.resumeWithException(CameraVideoCapturerException(errorDescription))
            }
        }
    }

    internal fun onAudioTrackStopped(trackId: String) {
        if (audioTracks.remove(trackId) == null) return

        if (audioTracks.isEmpty()) {
            audioSourceRef.getAndUpdate { null }?.also {
                GlobalScope.launch(Dispatchers.Default) { it.dispose() }
            }
        }
    }

    internal fun onVideoTrackStopped(trackId: String) {
        if (videoTracks.remove(trackId) == null) return

        if (videoTracks.isEmpty()) {
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