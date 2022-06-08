package com.shepeliev.webrtckmp.sample.shared

import co.touchlab.kermit.Logger
import co.touchlab.kermit.platformLogWriter
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.reduce
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.shepeliev.webrtckmp.*
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.ChangeType
import dev.gitlive.firebase.firestore.DocumentReference
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class RoomComponent(
    componentContext: ComponentContext,
    viewModel: Room = componentContext.instanceKeeper.getOrCreate { ViewModel() }
) : Room by viewModel, ComponentContext by componentContext {

    init {
        Logger.setLogWriters(platformLogWriter())
    }

    private class ViewModel : InstanceKeeper.Instance, Room {

        private val _model = MutableValue(Room.Model())
        override val model: Value<Room.Model> get() = _model

        private val scope = MainScope()
        private val firestore by lazy { Firebase.firestore }

        private var peerConnection: PeerConnection? = null

        override fun openUserMedia() {
            scope.launch {
                val stream = MediaDevices.getUserMedia(audio = true, video = true)
                _model.reduce { it.copy(localStream = stream) }
            }
        }

        override fun createRoom() {
            _model.reduce { it.copy(isJoining = true, isCaller = true) }
            val peerConnection = createPeerConnection()
            this@ViewModel.peerConnection = peerConnection

            val roomId = firestore.collection("rooms").document.id
            val roomRef = firestore.document("rooms/$roomId")
            collectIceCandidates(roomRef, peerConnection, "caller", "callee")

            scope.launch {
                val offer = peerConnection.createOffer(DefaultOfferAnswerOptions).also {
                    peerConnection.setLocalDescription(it)
                }

                roomRef.set(OfferMessage.serializer(), OfferMessage(offer.sdp))

                _model.reduce { it.copy(roomId = roomRef.id, isJoining = false) }

                roomRef.snapshots
                    .map { it.data(AnswerMessage.serializer()) }
                    .onEach { Logger.i { "Answer received: $it" } }
                    .filter { peerConnection.remoteDescription == null && it.answer != null }
                    .map {
                        SessionDescription(
                            type = SessionDescriptionType.Answer,
                            sdp = it.answer!!
                        )
                    }
                    .onEach { peerConnection.setRemoteDescription(it) }
                    .launchIn(scope)
            }
        }

        override fun joinRoom(roomId: String) {
            _model.reduce { it.copy(isJoining = true, roomId = roomId, isCaller = false) }

            val peerConnection = createPeerConnection()
            this@ViewModel.peerConnection = peerConnection

            val roomRef = firestore.document("rooms/$roomId")

            collectIceCandidates(roomRef, peerConnection, "callee", "caller")

            scope.launch {
                val roomDoc = roomRef.get()
                val offerMessage = roomDoc.data(OfferMessage.serializer())
                if (offerMessage.offer == null) {
                    Logger.e { "No offer SDP in the room [id = $roomId]" }
                    _model.reduce { it.copy(isJoining = false, isCaller = null) }
                    return@launch
                }

                val offer = SessionDescription(
                    type = SessionDescriptionType.Offer,
                    sdp = offerMessage.offer
                )
                peerConnection.setRemoteDescription(offer)
                peerConnection.createAnswer(DefaultOfferAnswerOptions).also {
                    peerConnection.setLocalDescription(it)
                    roomRef.update("answer" to it.sdp)
                }

                _model.reduce { it.copy(isJoining = false) }
            }
        }

        private fun createPeerConnection(): PeerConnection {
            Logger.i { "Create PeerConnection." }
            val localStream = checkNotNull(model.value.localStream)
            val peerConnection = PeerConnection(DefaultRtcConfig)
            peerConnection.addTrack(localStream.audioTracks.first(), localStream)
            peerConnection.addTrack(localStream.videoTracks.first(), localStream)
            listenRemoteTracks(peerConnection)
            registerListeners(peerConnection)
            return peerConnection
        }

        private fun registerListeners(peerConnection: PeerConnection) {
            peerConnection.onIceGatheringState
                .onEach { Logger.i { "ICE gathering state changed: $it" } }
                .launchIn(scope)

            peerConnection.onConnectionStateChange
                .onEach { Logger.i { "Connection state changed: $it" } }
                .launchIn(scope)

            peerConnection.onSignalingStateChange
                .onEach { Logger.i { "Signaling state changed: $it" } }
                .launchIn(scope)

            peerConnection.onIceConnectionStateChange
                .onEach { Logger.i { "ICE connection state changed: $it" } }
                .launchIn(scope)
        }

        private fun listenRemoteTracks(peerConnection: PeerConnection) {
            peerConnection.onTrack
                .onEach { Logger.i { "Remote track received: [id = ${it.track?.id}, kind: ${it.track?.kind} ]" } }
                .filter { it.track?.kind == MediaStreamTrackKind.Video }
                .onEach { event -> _model.reduce { it.copy(remoteStream = event.streams.first()) } }
                .launchIn(scope)
        }

        private fun collectIceCandidates(
            roomRef: DocumentReference,
            peerConnection: PeerConnection,
            localName: String,
            remoteName: String
        ) {
            val candidatesCollection = roomRef.collection(localName)

            peerConnection.onIceCandidate
                .map {
                    IceCandidateMessage(
                        candidate = it.candidate,
                        sdpMLineIndex = it.sdpMLineIndex,
                        sdpMid = it.sdpMid
                    )
                }
                .onEach { Logger.i { "New local ICE candidate: $it" } }
                .onEach { candidatesCollection.add(IceCandidateMessage.serializer(), it) }
                .launchIn(scope)

            roomRef.collection(remoteName).snapshots
                .onEach { snapshot ->
                    snapshot.documentChanges.forEach { change ->
                        if (change.type == ChangeType.ADDED) {
                            val message = change.document.data(IceCandidateMessage.serializer())
                            peerConnection.addIceCandidate(
                                IceCandidate(
                                    sdpMid = message.sdpMid,
                                    sdpMLineIndex = message.sdpMLineIndex,
                                    candidate = message.candidate,
                                )
                            )

                            Logger.i { "New remote ICE candidate: $message" }
                        }
                    }
                }
                .launchIn(scope)
        }

        override fun hangup() {
            _model.value.localStream?.release()
            peerConnection?.close()
            _model.reduce { Room.Model() }
        }

        override fun onDestroy() {
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

@Serializable
private data class OfferMessage(val offer: String? = null)

@Serializable
private data class AnswerMessage(val answer: String? = null)

@Serializable
private data class IceCandidateMessage(
    val candidate: String,
    val sdpMLineIndex: Int,
    val sdpMid: String
)
