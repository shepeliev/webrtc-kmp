package com.shepeliev.webrtckmp.sample.shared

import co.touchlab.kermit.Logger
import co.touchlab.kermit.platformLogWriter
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.reduce
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnPause
import com.arkivanov.essenty.lifecycle.doOnResume
import com.arkivanov.essenty.lifecycle.doOnStart
import com.arkivanov.essenty.lifecycle.doOnStop
import com.shepeliev.webrtckmp.IceServer
import com.shepeliev.webrtckmp.MediaDevices
import com.shepeliev.webrtckmp.MediaStreamTrack
import com.shepeliev.webrtckmp.MediaStreamTrackKind
import com.shepeliev.webrtckmp.OfferAnswerOptions
import com.shepeliev.webrtckmp.PeerConnection
import com.shepeliev.webrtckmp.RtcConfiguration
import com.shepeliev.webrtckmp.audioTracks
import com.shepeliev.webrtckmp.onConnectionStateChange
import com.shepeliev.webrtckmp.onIceCandidate
import com.shepeliev.webrtckmp.onIceConnectionStateChange
import com.shepeliev.webrtckmp.onIceGatheringState
import com.shepeliev.webrtckmp.onSignalingStateChange
import com.shepeliev.webrtckmp.onTrack
import com.shepeliev.webrtckmp.videoTracks
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class RoomComponent(
    componentContext: ComponentContext,
    viewModel: Room,
) : Room by viewModel, ComponentContext by componentContext {

    constructor(componentContext: ComponentContext) :
        this(componentContext, componentContext.instanceKeeper.getOrCreate { ViewModel() })

    private val logger = Logger.withTag("RoomComponent")

    init {
        Logger.setLogWriters(platformLogWriter())

        lifecycle.doOnCreate { logger.d { "onCreate" } }
        lifecycle.doOnStart { logger.d { "onStart" } }
        lifecycle.doOnResume { logger.d { "onResume" } }
        lifecycle.doOnPause { logger.d { "onPause" } }
        lifecycle.doOnStop { logger.d { "onStop" } }
        lifecycle.doOnDestroy { logger.d { "onDestroy" } }
    }

    private class ViewModel : InstanceKeeper.Instance, Room {

        private val logger = Logger.withTag("RoomComponent => ViewModel")
        private val _model = MutableValue(Room.Model())
        override val model: Value<Room.Model> get() = _model

        private val scope = MainScope()

        private val roomDataSource = RoomDataSource()
        private var peerConnection: PeerConnection? = null
        private var roomSessionJob: Job? = null

        override fun openUserMedia() {
            logger.i { "Open user media" }
            roomSessionJob = SupervisorJob()

            scope.launch {
                try {
                    val stream = MediaDevices.getUserMedia(audio = true, video = true)
                    _model.reduce { it.copy(localStream = stream) }
                    listenTrackState(stream.videoTracks.first(), "Local video")
                } catch (e: Throwable) {
                    logger.e(e) { "Getting user media failed" }
                }
            }
        }

        override fun switchCamera() {
            logger.i { "Switch camera" }
            scope.launch {
                model.value.localStream?.videoTracks?.first()?.switchCamera()
                logger.i { "Camera switched" }
            }
        }

        override fun createRoom() {
            logger.i { "Create room" }

            _model.reduce { it.copy(isJoining = true, isCaller = true) }
            val peerConnection = createPeerConnection()
            this@ViewModel.peerConnection = peerConnection

            scope.launch {
                val roomId = roomDataSource.createRoom()
                logger.d { "Room ID: $roomId" }

                collectIceCandidates(peerConnection, roomId, "caller", "callee")

                val offer = peerConnection.createOffer(DefaultOfferAnswerOptions).also {
                    peerConnection.setLocalDescription(it)
                }

                roomDataSource.insertOffer(roomId, offer)
                _model.reduce { it.copy(roomId = roomId, isJoining = false) }

                logger.d { "Waiting answer" }
                val answer = roomDataSource.getAnswer(roomId)
                logger.d { "Answer received." }
                peerConnection.setRemoteDescription(answer)
            }
        }

        override fun joinRoom(roomId: String) {
            logger.i { "Join room: $roomId" }

            _model.reduce { it.copy(isJoining = true, roomId = roomId, isCaller = false) }
            roomSessionJob = SupervisorJob()

            val peerConnection = createPeerConnection()
            this@ViewModel.peerConnection = peerConnection

            scope.launch {
                val offer = roomDataSource.getOffer(roomId)
                if (offer == null) {
                    logger.e { "No offer SDP in the room [id = $roomId]" }
                    _model.reduce { it.copy(isJoining = false, isCaller = null) }
                    return@launch
                }

                collectIceCandidates(peerConnection, roomId, "callee", "caller")

                peerConnection.setRemoteDescription(offer)
                peerConnection.createAnswer(DefaultOfferAnswerOptions).also {
                    peerConnection.setLocalDescription(it)
                    roomDataSource.insertAnswer(roomId, it)
                }

                _model.reduce { it.copy(isJoining = false) }
            }
        }

        private fun createPeerConnection(): PeerConnection {
            logger.i { "Create PeerConnection." }
            val peerConnection = PeerConnection(DefaultRtcConfig)

            model.value.localStream?.let {
                peerConnection.addTrack(it.audioTracks.first(), it)
                peerConnection.addTrack(it.videoTracks.first(), it)
            }

            listenRemoteTracks(peerConnection)
            registerListeners(peerConnection)
            return peerConnection
        }

        private fun registerListeners(peerConnection: PeerConnection) {
            peerConnection.onIceGatheringState
                .onEach { logger.i { "ICE gathering state changed: $it" } }
                .launchIn(scope + roomSessionJob!!)

            peerConnection.onConnectionStateChange
                .onEach { logger.i { "Connection state changed: $it" } }
                .launchIn(scope + roomSessionJob!!)

            peerConnection.onSignalingStateChange
                .onEach { logger.i { "Signaling state changed: $it" } }
                .launchIn(scope + roomSessionJob!!)

            peerConnection.onIceConnectionStateChange
                .onEach { logger.i { "ICE connection state changed: $it" } }
                .launchIn(scope + roomSessionJob!!)
        }

        private fun listenRemoteTracks(peerConnection: PeerConnection) {
            peerConnection.onTrack
                .onEach { logger.i { "Remote track received: [id = ${it.track?.id}, kind: ${it.track?.kind} ]" } }
                .filter { it.track?.kind == MediaStreamTrackKind.Video }
                .onEach { event -> _model.reduce { it.copy(remoteStream = event.streams.first()) } }
                .onEach { listenTrackState(it.track!!, "Remote video") }
                .launchIn(scope + roomSessionJob!!)
        }

        private fun listenTrackState(track: MediaStreamTrack, logPrefix: String) {
            track.state
                .onEach { logger.w { "$logPrefix track [id = ${track.id}] state changed: $it" } }
                .launchIn(scope + roomSessionJob!!)
        }

        private fun collectIceCandidates(
            peerConnection: PeerConnection,
            roomId: String,
            localName: String,
            remoteName: String
        ) {
            peerConnection.onIceCandidate
                .onEach { logger.i { "New local ICE candidate: $it" } }
                .onEach { roomDataSource.insertIceCandidate(roomId, localName, it) }
                .launchIn(scope + roomSessionJob!!)

            roomDataSource.observeIceCandidates(roomId, remoteName)
                .catch { logger.e(it) { "Observing ice candidate failed [roomId = $roomId, peerName = $remoteName]" } }
                .onEach { logger.d { "New remote ICE candidate: $it" } }
                .onEach(peerConnection::addIceCandidate)
                .launchIn(scope + roomSessionJob!!)
        }

        override fun hangup() {
            logger.i { "Hangup" }

            roomSessionJob?.cancel()
            roomSessionJob = null
            _model.value.localStream?.release()
            _model.reduce { Room.Model() }

            peerConnection?.close()
            peerConnection = null
        }

        override fun onDestroy() {
            logger.i { "Destroy" }
            scope.cancel()
        }
    }
}

private val DefaultRtcConfig = RtcConfiguration(
    iceServers = listOf(
        IceServer(listOf("stun:stun1.l.google.com:19302", "stun:stun2.l.google.com:19302")),
    )
)

private val DefaultOfferAnswerOptions = OfferAnswerOptions(
    offerToReceiveVideo = true,
    offerToReceiveAudio = true,
)
