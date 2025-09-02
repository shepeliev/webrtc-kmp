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
import org.webrtc.VideoProcessor

@Suppress("MemberVisibilityCanBePrivate")
public object WebRtc {
    private var _rootEglBase: EglBase? = null

    /**
     * The root [EglBase] instance.
     */
    public val rootEglBase: EglBase by lazy {
        _rootEglBase ?: EglBase.create().also { _rootEglBase = it }
    }

    internal lateinit var applicationContext: Context
        private set

    internal var videoProcessorFactory: VideoProcessorFactory? = null
        private set

    @Suppress("ktlint:standard:backing-property-naming")
    private var _cameraEnumerator: CameraEnumerator? = null
    internal val cameraEnumerator: CameraEnumerator by lazy {
        _cameraEnumerator ?: if (Camera2Enumerator.isSupported(applicationContext)) {
            Camera2Enumerator(applicationContext)
        } else {
            Camera1Enumerator()
        }
    }

    @Suppress("ktlint:standard:backing-property-naming")
    private var _peerConnectionFactory: PeerConnectionFactory? = null
    internal val peerConnectionFactory: PeerConnectionFactory by lazy {
        _peerConnectionFactory ?: createPeerConnectionFactoryBuilder()
            .createPeerConnectionFactory()
            .also { _peerConnectionFactory = it }
    }

    /**
     * Creates a new default [InitializationOptions.Builder].
     */
    public fun createInitializationOptionsBuilder(): InitializationOptions.Builder =
        InitializationOptions.builder(applicationContext)

    /**
     * Creates a new default [PeerConnectionFactory.Builder].
     */
    public fun createPeerConnectionFactoryBuilder(
        rootEglBase: EglBase? = null,
        initializationOptionsBuilder: InitializationOptions.Builder =
            createInitializationOptionsBuilder(),
        enableIntelVp8Encoder: Boolean = true,
        enableH264HighProfile: Boolean = true,
    ): PeerConnectionFactory.Builder {
        if (rootEglBase != null) {
            check(_rootEglBase == null) { "Root EglBase is already initialized." }
            _rootEglBase = rootEglBase
        }

        PeerConnectionFactory.initialize(initializationOptionsBuilder.createInitializationOptions())

        val videoDecoderFactory = DefaultVideoDecoderFactory(this.rootEglBase.eglBaseContext)
        val defaultVideoEncoderFactory =
            DefaultVideoEncoderFactory(
                this.rootEglBase.eglBaseContext,
                enableIntelVp8Encoder,
                enableH264HighProfile,
            )
        return PeerConnectionFactory
            .builder()
            .setVideoDecoderFactory(videoDecoderFactory)
            .setVideoEncoderFactory(defaultVideoEncoderFactory)
    }

    /**
     * Configures the WebRTC library. This method must be called once only and before any access to
     * MediaDevices.
     *
     * @param videoProcessorFactory The factory to create [VideoProcessor] instances.
     * @param cameraEnumerator The camera enumerator to use. If not provided, the default enumerator
     * will be used.
     * @param peerConnectionFactoryBuilder The [PeerConnectionFactory.Builder] to use for creating
     * [PeerConnectionFactory].
     */
    @Suppress("unused")
    public fun configure(
        videoProcessorFactory: VideoProcessorFactory? = null,
        cameraEnumerator: CameraEnumerator? = null,
        peerConnectionFactoryBuilder: PeerConnectionFactory.Builder =
            createPeerConnectionFactoryBuilder(),
    ) {
        check(_peerConnectionFactory == null) {
            "WebRtc.configurePeerConnectionFactory() must be called once only and before any access to MediaDevices."
        }

        this.videoProcessorFactory = videoProcessorFactory
        _cameraEnumerator = cameraEnumerator
        _peerConnectionFactory = peerConnectionFactoryBuilder.createPeerConnectionFactory()
    }

    internal fun initializeApplicationContext(context: Context) {
        applicationContext = context.applicationContext
    }
}
