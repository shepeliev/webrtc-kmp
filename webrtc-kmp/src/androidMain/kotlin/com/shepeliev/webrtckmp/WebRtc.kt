@file:JvmName("WebRtcKmpAndroid")

package com.shepeliev.webrtckmp

import android.content.Context
import com.shepeliev.webrtckmp.capturer.CameraSelector
import com.shepeliev.webrtckmp.capturer.CameraVideoCapturerFactory
import org.webrtc.Camera1Enumerator
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraEnumerator
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.PeerConnectionFactory
import org.webrtc.PeerConnectionFactory.InitializationOptions
import org.webrtc.PeerConnectionFactory.Options
import org.webrtc.VideoDecoderFactory
import org.webrtc.VideoEncoderFactory
import org.webrtc.audio.AudioDeviceModule

@Suppress("MemberVisibilityCanBePrivate")
object WebRtc {
    private var _rootEglBase: EglBase? = null
    val rootEglBase: EglBase by lazy {
        _rootEglBase ?: EglBase.create().also { _rootEglBase = it }
    }

    private var factoryInitializationOptions: InitializationOptions? = null
    private var options: Options? = null
    private var audioDeviceModule: AudioDeviceModule? = null
    private var videoEncoderFactory: VideoEncoderFactory? = null
    private var videoDecoderFactory: VideoDecoderFactory? = null

    internal lateinit var applicationContext: Context
        private set

    internal var videoProcessorFactory: VideoProcessorFactory? = null
        private set

    internal var cameraVideoCapturerFactory: CameraVideoCapturerFactory =
        CameraVideoCapturerFactory()
        private set

    internal var cameraSelector: CameraSelector = CameraSelector()
        private set

    private var _cameraEnumerator: CameraEnumerator? = null
    internal val cameraEnumerator: CameraEnumerator by lazy {
        _cameraEnumerator ?: if (Camera2Enumerator.isSupported(applicationContext)) {
            Camera2Enumerator(applicationContext)
        } else {
            Camera1Enumerator()
        }
    }

    internal val peerConnectionFactory: PeerConnectionFactory by lazy {
        val initializationOptions = factoryInitializationOptions
            ?: InitializationOptions.builder(applicationContext).createInitializationOptions()
        PeerConnectionFactory.initialize(initializationOptions)

        val videoDecoderFactory =
            videoDecoderFactory ?: DefaultVideoDecoderFactory(rootEglBase.eglBaseContext)

        val videoEncoderFactory = videoEncoderFactory ?: DefaultVideoEncoderFactory(
            /* eglContext = */ rootEglBase.eglBaseContext,
            /* enableIntelVp8Encoder = */ true,
            /* enableH264HighProfile = */ true
        )

        PeerConnectionFactory.builder()
            .setOptions(options)
            .setAudioDeviceModule(audioDeviceModule)
            .setVideoDecoderFactory(videoDecoderFactory)
            .setVideoEncoderFactory(videoEncoderFactory)
            .createPeerConnectionFactory()
    }

    @Suppress("unused")
    fun configurePeerConnectionFactory(
        rootEglBase: EglBase? = null,
        peerConnectionFactoryInitializationOptions: InitializationOptions? = null,
        options: Options? = null,
        audioDeviceModule: AudioDeviceModule? = null,
        videoEncoderFactory: VideoEncoderFactory? = null,
        videoDecoderFactory: VideoDecoderFactory? = null,
        cameraEnumerator: CameraEnumerator? = null,
        videoProcessorFactory: VideoProcessorFactory? = null,
        cameraVideoCapturerFactory: CameraVideoCapturerFactory = CameraVideoCapturerFactory(),
        cameraSelector: CameraSelector = CameraSelector()
    ) {
        check(_rootEglBase == null) {
            "WebRtc.configurePeerConnectionFactory() must be called once only and before any access to MediaDevices."
        }

        this._rootEglBase = rootEglBase ?: EglBase.create()
        this.factoryInitializationOptions = peerConnectionFactoryInitializationOptions
        this.options = options
        this.audioDeviceModule = audioDeviceModule
        this.videoEncoderFactory = videoEncoderFactory
        this.videoDecoderFactory = videoDecoderFactory
        this._cameraEnumerator = cameraEnumerator
        this.videoProcessorFactory = videoProcessorFactory
        this.cameraVideoCapturerFactory = cameraVideoCapturerFactory
        this.cameraSelector = cameraSelector
    }

    internal fun initializeApplicationContext(context: Context) {
        applicationContext = context.applicationContext
    }
}
