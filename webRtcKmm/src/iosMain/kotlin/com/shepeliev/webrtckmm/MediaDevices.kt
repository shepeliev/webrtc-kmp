//package com.shepeliev.webrtckmm
//
//import cocoapods.GoogleWebRTC.RTCCameraVideoCapturer
//import com.shepeliev.webrtckmm.utils.uuid
//import platform.AVFoundation.AVCaptureDevice
//import platform.AVFoundation.AVCaptureDevicePositionBack
//import platform.AVFoundation.AVCaptureDevicePositionFront
//import platform.AVFoundation.position
//import platform.Foundation.NSLog
//import kotlin.coroutines.cancellation.CancellationException
//
//@ThreadLocal
//actual object MediaDevices {
//    private const val tag = "MediaDevices"
//
//    private var audioSource: AudioSource? = null
//    private var videoSource: VideoSource? = null
//    private var cameraCapturer: CameraVideoCapturer? = null
//    private val audioTracks = mutableMapOf<String, Unit>()
//    private val videoTracks = mutableMapOf<String, Unit>()
//
//    // TODO implement video constraints
//    @Throws(CameraVideoCapturerException::class, CancellationException::class)
//    actual suspend fun getUserMedia(audio: Boolean, video: Boolean): MediaStream {
//        val factory = peerConnectionFactory
//        var audioTrack: AudioTrack? = null
//        if (audio) {
//            val source = audioSource ?: factory.createAudioSource(mediaConstraints())
//            audioSource = source
//            audioTrack = factory.createAudioTrack(uuid(), source)
//            audioTracks += audioTrack.id to Unit
//        }
//
//        var videoTrack: VideoTrack? = null
//        if (video) {
//            val source = videoSource ?: factory.createVideoSource(
//                isScreencast = false,
//                alignTimestamps = true
//            )
//            videoSource = source
//            if (cameraCapturer == null) {
//                cameraCapturer = CameraEnumerator.createCameraVideoCapturer(source).apply {
//                    startCapture(VideoConstraints())
//                }
//            }
//            videoTrack = factory.createVideoTrack(uuid(), source)
//            videoTracks += videoTrack.id to Unit
//        }
//
//        return factory.createLocalMediaStream(uuid()).apply {
//            audioTrack?.let { addTrack(it) }
//            videoTrack?.let { addTrack(it) }
//        }
//    }
//
//    actual suspend fun enumerateDevices(): List<MediaDeviceInfo> {
//        return CameraEnumerator.enumerateDevices()
//    }
//
//    @Throws(CameraVideoCapturerException::class, CancellationException::class)
//    actual suspend fun switchCamera(): MediaDeviceInfo {
//        val capturer =
//            cameraCapturer ?: throw CameraVideoCapturerException("Camera video capturer is stopped")
//        return capturer.switchCamera()
//    }
//
//    @Throws(CameraVideoCapturerException::class, CancellationException::class)
//    actual suspend fun switchCamera(cameraId: String): MediaDeviceInfo {
//        val capturer = cameraCapturer
//            ?: throw CameraVideoCapturerException("Camera video capturer is stopped")
//        return capturer.switchCamera(cameraId)
//    }
//
//    internal fun onAudioTrackStopped(trackId: String) {
//        if (audioTracks.remove(trackId) == null) return
//
//        if (audioTracks.isEmpty()) {
//            NSLog("$tag: There is no any active audio track. Dispose audio source.")
//            audioSource = null
//        }
//    }
//
//    internal fun onVideoTrackStopped(trackId: String) {
//        if (videoTracks.remove(trackId) == null) return
//
//        if (videoTracks.isEmpty()) {
//            cameraCapturer?.stopCapture()
//            cameraCapturer = null
//            videoSource = null
//        }
//    }
//}