@file:JvmName("WebRtcKmpAndroid")

package com.shepeliev.webrtckmp

import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.Logging
import org.webrtc.PeerConnectionFactory

object WebRtc {
    private const val TAG = "WebRtcKmp"

    var peerConnectionFactoryInitOptions: PeerConnectionFactory.InitializationOptions? = null
        set(value) {
            field = value
            if (_eglBase != null) {
                Logging.e(
                    TAG,
                    "Peer connection factory is already initialized. Setting " +
                        "peerConnectionFactoryInitOptions after initialization has no effect."
                )
            }
        }

    var peerConnectionFactoryBuilder: PeerConnectionFactory.Builder? = null
        set(value) {
            field = value
            if (_peerConnectionFactory != null) {
                Logging.e(
                    TAG,
                    "Peer connection factory is already initialized. Setting " +
                        "peerConnectionFactoryBuilder after initialization has no effect."
                )
            }
        }

    private var _eglBase: EglBase? = null
    val rootEglBase: EglBase
        get() {
            if (_eglBase == null) initialize()
            return checkNotNull(_eglBase)
        }

    private var _peerConnectionFactory: PeerConnectionFactory? = null
    internal val peerConnectionFactory: PeerConnectionFactory
        get() {
            if (_peerConnectionFactory == null) initialize()
            return checkNotNull(_peerConnectionFactory)
        }

    private fun initialize() {
        check(_eglBase == null) { "Peer connection factory is already initialized." }
        _eglBase = EglBase.create()
        initializePeerConnectionFactory()
        val builder = peerConnectionFactoryBuilder ?: getDefaultPeerConnectionBuilder()
        _peerConnectionFactory = builder.createPeerConnectionFactory()
    }

    private fun initializePeerConnectionFactory() {
        val initOptions = peerConnectionFactoryInitOptions
            ?: getDefaultPeerConnectionFactoryInitOptions()
        PeerConnectionFactory.initialize(initOptions)
    }

    private fun getDefaultPeerConnectionFactoryInitOptions() =
        PeerConnectionFactory.InitializationOptions
            .builder(ApplicationContextHolder.context)
            .createInitializationOptions()

    private fun getDefaultPeerConnectionBuilder(): PeerConnectionFactory.Builder {
        val videoEncoderFactory = DefaultVideoEncoderFactory(
            rootEglBase.eglBaseContext,
            true,
            false
        )

        val videoDecoderFactory = DefaultVideoDecoderFactory(rootEglBase.eglBaseContext)
        return PeerConnectionFactory.builder()
            .setVideoEncoderFactory(videoEncoderFactory)
            .setVideoDecoderFactory(videoDecoderFactory)
    }

    fun disposePeerConnectionFactory() {
        if (_peerConnectionFactory == null) return

        _eglBase?.release()
        _eglBase = null

        _peerConnectionFactory?.dispose()
        _peerConnectionFactory = null

        PeerConnectionFactory.shutdownInternalTracer()
    }
}
