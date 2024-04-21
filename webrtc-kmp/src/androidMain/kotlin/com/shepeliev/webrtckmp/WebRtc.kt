@file:JvmName("WebRtcKmpAndroid")

package com.shepeliev.webrtckmp

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
    var videoEncoderFactory: VideoEncoderFactory? = null
    var videoDecoderFactory: VideoDecoderFactory? = null
    var customPeerConnectionFactory: PeerConnectionFactory? = null
    lateinit var factoryInitializationOptionsBuilder: PeerConnectionFactory.InitializationOptions.Builder
        internal set

    private var _rootEglBase: EglBase? = null
    val rootEglBase: EglBase by lazy {
        _rootEglBase ?: EglBase.create().also { _rootEglBase = it }
    }

    var cameraEnumerator: CameraEnumerator =
        if (Camera2Enumerator.isSupported(ApplicationContextHolder.context)) {
            Camera2Enumerator(ApplicationContextHolder.context)
        } else {
            Camera1Enumerator()
        }

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

    @Suppress("unused")
    fun setRootEglBase(eglBase: EglBase) {
        check(_rootEglBase == null) { "Root EglBase is already set" }
        _rootEglBase = eglBase
    }
}
