@file:JvmName("WebRtcKmpAndroid")

package com.shepeliev.webrtckmp

import android.content.Context
import org.webrtc.Camera1Enumerator
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraEnumerator
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.PeerConnectionFactory
import org.webrtc.VideoDecoderFactory
import org.webrtc.VideoEncoderFactory

@Suppress("MemberVisibilityCanBePrivate")
object WebRtc {
    private var _rootEglBase: EglBase? = null
    val rootEglBase: EglBase by lazy {
        _rootEglBase ?: EglBase.create().also { _rootEglBase = it }
    }

    var videoEncoderFactory: VideoEncoderFactory? = null
    var videoDecoderFactory: VideoDecoderFactory? = null
    var customCameraEnumerator: CameraEnumerator? = null
    var customPeerConnectionFactory: PeerConnectionFactory? = null
    var videoProcessorFactory: VideoProcessorFactory? = null

    lateinit var factoryInitializationOptionsBuilder: PeerConnectionFactory.InitializationOptions.Builder
        private set

    internal lateinit var applicationContext: Context
        private set

    internal val peerConnectionFactory: PeerConnectionFactory by lazy {
        customPeerConnectionFactory ?: run {
            PeerConnectionFactory.initialize(factoryInitializationOptionsBuilder.createInitializationOptions())

            val videoEncoderFactory = videoEncoderFactory
                ?: DefaultVideoEncoderFactory(rootEglBase.eglBaseContext, true, true)
            val videoDecoderFactory = videoDecoderFactory ?: DefaultVideoDecoderFactory(rootEglBase.eglBaseContext)

            val factoryBuilder = PeerConnectionFactory.builder()
                .setVideoEncoderFactory(videoEncoderFactory)
                .setVideoDecoderFactory(videoDecoderFactory)

            factoryBuilder.createPeerConnectionFactory()
        }
    }

    internal val cameraEnumerator: CameraEnumerator by lazy {
        customCameraEnumerator ?: if (Camera2Enumerator.isSupported(applicationContext)) {
            Camera2Enumerator(applicationContext)
        } else {
            Camera1Enumerator()
        }
    }

    @Suppress("unused")
    fun setRootEglBase(eglBase: EglBase) {
        check(_rootEglBase == null) { "Root EglBase is already set" }
        _rootEglBase = eglBase
    }

    internal fun initialize(context: Context) {
        applicationContext = context.applicationContext
        factoryInitializationOptionsBuilder = PeerConnectionFactory.InitializationOptions.builder(context)
    }
}
