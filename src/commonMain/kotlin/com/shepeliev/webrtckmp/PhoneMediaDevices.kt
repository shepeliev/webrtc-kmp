package com.shepeliev.webrtckmp

import com.shepeliev.webrtckmp.utils.uuid
import kotlin.coroutines.cancellation.CancellationException
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
internal object PhoneMediaDevices {

    private var audioSource: AudioSource? = null
    private var audioSourceRefCount = 0

    private val videoCapturer = CameraVideoCapturer()
    private var videoSource: VideoSource? = null
    private var videoSourceRef = 0

    fun getUserMedia(audio: Boolean, video: Boolean): MediaStream {
            val factory = WebRtcKmp.peerConnectionFactory
            var audioTrack: AudioStreamTrack? = null
            if (audio) {
                audioSource =
                    audioSource ?: factory.createAudioSource(mediaConstraints( /* TODO */))
                audioSourceRefCount += 1
                audioTrack = factory.createAudioTrack(uuid(), audioSource!!)
            }

            var videoTrack: VideoStreamTrack? = null
            if (video) {
                videoCapturer.stopCapture()

                val device = CameraEnumerator.selectDevice(VideoConstraints( /* TODO */))

                videoSource = videoSource ?: factory.createVideoSource(false)
                videoSourceRef += 1
                videoTrack = factory.createVideoTrack(uuid(), videoSource!!)

                videoCapturer.startCapture(
                    device.deviceId,
                    VideoConstraints( /* TODO */),
                    videoSource!!
                )
            }

            return MediaStream().apply {
                if (audioTrack != null) addTrack(audioTrack)
                if (videoTrack != null) addTrack(videoTrack)
            }
    }

    internal fun onAudioTrackStopped() {
        check(audioSourceRefCount > 0)
        audioSourceRefCount -= 1
        if (audioSourceRefCount == 0) {
            audioSource?.dispose()
            audioSource = null
        }
    }

    internal fun onVideoTrackStopped() {
        check(videoSourceRef > 0)
        videoSourceRef -= 1
        if (videoSourceRef == 0) {
            videoCapturer.stopCapture()
            videoSource?.dispose()
            videoSource = null
        }
    }

    @Throws(CameraVideoCapturerException::class, CancellationException::class)
    suspend fun switchCamera() = videoCapturer.switchCamera()

    @Throws(CameraVideoCapturerException::class, CancellationException::class)
    suspend fun switchCamera(cameraId: String) = videoCapturer.switchCamera(cameraId)
}

const val DEFAULT_VIDEO_WIDTH = 1280
const val DEFAULT_VIDEO_HEIGHT = 720
const val DEFAULT_FRAME_RATE = 30

data class VideoConstraints(
    val deviceId: String? = null,
    val isFrontFacing: Boolean? = null,
    val width: Int? = null,
    val height: Int? = null,
    val fps: Int? = null,
)
