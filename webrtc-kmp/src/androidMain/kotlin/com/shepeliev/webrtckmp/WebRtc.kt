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
import org.webrtc.PeerConnectionFactory.InitializationOptions

@Suppress("MemberVisibilityCanBePrivate")
object WebRtc {
    private var _rootEglBase: EglBase? = null
    val rootEglBase: EglBase by lazy {
        _rootEglBase ?: EglBase.create().also { _rootEglBase = it }
    }

    internal lateinit var applicationContext: Context
        private set

    internal var videoProcessorFactory: VideoProcessorFactory? = null
        private set

    private var _cameraEnumerator: CameraEnumerator? = null
    internal val cameraEnumerator: CameraEnumerator by lazy {
        _cameraEnumerator ?: if (Camera2Enumerator.isSupported(applicationContext)) {
            Camera2Enumerator(applicationContext)
        } else {
            Camera1Enumerator()
        }
    }

    private var _peerConnectionFactory: PeerConnectionFactory? = null
    internal val peerConnectionFactory: PeerConnectionFactory by lazy {
        _peerConnectionFactory ?: createPeerConnectionFactory(
            defaultPeerConnectionFactoryInitializationOptionsBuilder(),
            defaultPeerConnectionBuilder()
        ).also { _peerConnectionFactory = it }
    }

    @Suppress("unused")
    fun configure(
        rootEglBase: EglBase? = null,
        videoProcessorFactory: VideoProcessorFactory? = null,
        cameraEnumerator: CameraEnumerator? = null,
        peerConnectionInitializationOptionsBuilder: InitializationOptions.Builder = defaultPeerConnectionFactoryInitializationOptionsBuilder(),
        peerConnectionFactoryBuilder: PeerConnectionFactory.Builder = defaultPeerConnectionBuilder(),
    ) {
        check(_peerConnectionFactory == null) {
            "WebRtc.configurePeerConnectionFactory() must be called once only and before any access to MediaDevices."
        }

        if (rootEglBase != null) {
            check(_rootEglBase == null) { "Root EglBase is already initialized." }
            _rootEglBase = rootEglBase
        }

        this.videoProcessorFactory = videoProcessorFactory
        _cameraEnumerator = cameraEnumerator
        _peerConnectionFactory = createPeerConnectionFactory(
            peerConnectionInitializationOptionsBuilder,
            peerConnectionFactoryBuilder
        )
    }

    internal fun initializeApplicationContext(context: Context) {
        applicationContext = context.applicationContext
    }

    private fun createPeerConnectionFactory(
        peerConnectionInitializationOptionsBuilder: InitializationOptions.Builder,
        peerConnectionFactoryBuilder: PeerConnectionFactory.Builder
    ): PeerConnectionFactory {
        PeerConnectionFactory.initialize(peerConnectionInitializationOptionsBuilder.createInitializationOptions())
        return peerConnectionFactoryBuilder.createPeerConnectionFactory()
    }

    private fun defaultPeerConnectionFactoryInitializationOptionsBuilder(): InitializationOptions.Builder {
        return InitializationOptions.builder(applicationContext)
    }

    private fun defaultPeerConnectionBuilder(): PeerConnectionFactory.Builder {
        return PeerConnectionFactory.builder()
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(rootEglBase.eglBaseContext))
            .setVideoEncoderFactory(
                DefaultVideoEncoderFactory(
                    rootEglBase.eglBaseContext,
                    true,
                    true
                )
            )
    }
}
