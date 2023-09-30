@file:JvmName("WebRtcKmpAndroid")

package com.shepeliev.webrtckmp

import org.webrtc.Camera1Enumerator
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraEnumerator
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.Logging
import org.webrtc.PeerConnectionFactory

object WebRtc {
    private const val TAG = "WebRtcKmp"

    var peerConnectionFactoryInitOptions: PeerConnectionFactory.InitializationOptions? = null
        set(value) {
            if (_peerConnectionFactory != null) {
                Logging.e(
                    TAG,
                    "Peer connection factory is already initialized. Setting " +
                        "peerConnectionFactoryInitOptions after initialization has no effect."
                )
            } else {
                field = value
            }
        }

    var peerConnectionFactoryBuilder: () -> PeerConnectionFactory.Builder = ::getDefaultPeerConnectionBuilder
        set(value) {
            if (_peerConnectionFactory != null) {
                Logging.e(
                    TAG,
                    "Peer connection factory is already initialized. Setting " +
                        "peerConnectionFactoryBuilder after initialization has no effect."
                )
            } else {
                field = value
            }
        }

    private var _eglBase: EglBase? = null
    val rootEglBase: EglBase
        get() {
            if (_eglBase == null) {
                initializePeerConnectionFactory()
                _eglBase = EglBase.create()
            }
            return checkNotNull(_eglBase)
        }

    var cameraEnumerator: CameraEnumerator =
        if (Camera2Enumerator.isSupported(ApplicationContextHolder.context)) {
            Camera2Enumerator(ApplicationContextHolder.context)
        } else {
            Camera1Enumerator()
        }

    private var _peerConnectionFactory: PeerConnectionFactory? = null
    internal val peerConnectionFactory: PeerConnectionFactory
        get() {
            if (_peerConnectionFactory == null) createPeerConnectionFactory()
            return checkNotNull(_peerConnectionFactory)
        }

    private fun createPeerConnectionFactory() {
        check(_peerConnectionFactory == null) { "Peer connection factory is already initialized." }
        val builder = peerConnectionFactoryBuilder()
        _peerConnectionFactory = builder.createPeerConnectionFactory()
    }

    private fun initializePeerConnectionFactory() {
        val initOptions = peerConnectionFactoryInitOptions ?: getDefaultPeerConnectionFactoryInitOptions()
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

    @Suppress("unused")
    fun disposePeerConnectionFactory() {
        if (_peerConnectionFactory == null) return

        _eglBase?.release()
        _eglBase = null

        _peerConnectionFactory?.dispose()
        _peerConnectionFactory = null

        PeerConnectionFactory.shutdownInternalTracer()
    }
}
