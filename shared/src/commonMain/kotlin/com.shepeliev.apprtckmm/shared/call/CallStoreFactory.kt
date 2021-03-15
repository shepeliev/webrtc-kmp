package com.shepeliev.apprtckmm.shared.call

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendExecutor
import com.shepeliev.apprtckmm.shared.Log
import com.shepeliev.apprtckmm.shared.RoomConnectionParameters
import com.shepeliev.apprtckmm.shared.SignalingParameters
import com.shepeliev.apprtckmm.shared.call.CallStore.Intent
import com.shepeliev.apprtckmm.shared.call.CallStore.Intent.Disconnect
import com.shepeliev.apprtckmm.shared.call.CallStore.Label
import com.shepeliev.apprtckmm.shared.call.CallStore.State
import com.shepeliev.apprtckmm.shared.call.CallStoreFactory.Action.IceCandidateReady
import com.shepeliev.apprtckmm.shared.call.CallStoreFactory.Action.IceCandidatesRemoved
import com.shepeliev.apprtckmm.shared.call.CallStoreFactory.Action.JoinRoom
import com.shepeliev.apprtckmm.shared.call.CallStoreFactory.Action.LocalDescriptionReady
import com.shepeliev.apprtckmm.shared.call.CallStoreFactory.Action.RemoteDescriptionReady
import com.shepeliev.apprtckmm.shared.call.CallStoreFactory.Result.LocalVideoTrack
import com.shepeliev.apprtckmm.shared.call.CallStoreFactory.Result.RemoteVideoTrack
import com.shepeliev.apprtckmm.shared.rtcclient.AppRtcClient
import com.shepeliev.apprtckmm.shared.rtcclient.PeerConnectionClient
import com.shepeliev.apprtckmm.shared.rtcclient.WebSocketRtcClient
import com.shepeliev.webrtckmm.IceCandidate
import com.shepeliev.webrtckmm.RtcStatsReport
import com.shepeliev.webrtckmm.SessionDescription
import com.shepeliev.webrtckmm.VideoTrack

internal class CallStoreFactory(private val storeFactory: StoreFactory) {

    fun create(joinRoom: JoinRoom): CallStore =
        object : CallStore, Store<Intent, State, Label> by storeFactory.create(
            name = "CallStore",
            initialState = State(),
            bootstrapper = SimpleBootstrapper(joinRoom),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl,
        ) {}

    sealed class Action {
        data class JoinRoom(val roomUrl: String, val roomId: String) : Action()
        internal data class IceCandidateReady(val candidate: IceCandidate) : Action()
        internal class IceCandidatesRemoved(val candidates: List<IceCandidate>) : Action()
        internal class LocalDescriptionReady(val description: SessionDescription) : Action()
        internal class RemoteDescriptionReady(val description: SessionDescription) : Action()
    }

    private sealed class Result {
        data class LocalVideoTrack(val track: VideoTrack) : Result()
        data class RemoteVideoTrack(val track: VideoTrack) : Result()
    }

    private object ReducerImpl : Reducer<State, Result> {
        override fun State.reduce(result: Result): State {
            return when (result) {
                is LocalVideoTrack -> copy(localVideoTrack = result.track)
                is RemoteVideoTrack -> copy(remoteVideoTrack = result.track)
            }
        }
    }

    private class ExecutorImpl : SuspendExecutor<Intent, Action, State, Result, Label>() {

        private val tag = "CallViewModel"
        private val client: AppRtcClient = WebSocketRtcClient(SignalingEvents())
        private val pcClient = PeerConnectionClient(PeerConnectionEvents())
        private lateinit var signalingParams: SignalingParameters

        override suspend fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {
                is Disconnect -> {
                    client.disconnectFromRoom()
                    pcClient.close()
                    publish(Label.Disconnected)
                }
                Intent.SwitchCamera -> pcClient.switchCamera()
            }
        }

        override suspend fun executeAction(action: Action, getState: () -> State) {
            when (action) {
                is JoinRoom -> joinRoom(action)
                is IceCandidateReady -> client.sendLocalIceCandidate(action.candidate)
                is IceCandidatesRemoved ->  client.sendLocalIceCandidateRemovals(action.candidates)
                is LocalDescriptionReady -> executeLocalDescriptionReadyAction(action)
                is RemoteDescriptionReady -> executeRemoteDescriptionReadyAction(action)
            }
        }

        private suspend fun executeRemoteDescriptionReadyAction(action: RemoteDescriptionReady) {
            val description = action.description
            log("Received remote ${description.type}")
            pcClient.setRemoteDescription(description)
            if (!signalingParams.initiator) {
                pcClient.createAnswer()
            }
        }

        private suspend fun executeLocalDescriptionReadyAction(action: LocalDescriptionReady) {
            val description = action.description
            log("Sending ${description.type}")
            if (signalingParams.initiator) {
                client.sendOfferSdp(description)
            } else {
                client.sendAnswerSdp(description)
            }
        }

        private suspend fun joinRoom(action: JoinRoom) {
            val (roomUrl, roomId) = action
            val params = RoomConnectionParameters(roomUrl, roomId, false)

            try {
                signalingParams = client.connectToRoom(params)
            } catch (e: Throwable) {
                Log.e(tag, "Connection failed", e)
                disconnectWithErrorMessage("Connecting room failed: ${e.message}")
                return
            }

            pcClient.createPeerConnection(signalingParams)
            if (signalingParams.initiator) {
                log("Creating OFFER...")
                pcClient.createOffer()
            } else {
                if (signalingParams.offerSdp != null) {
                    pcClient.setRemoteDescription(signalingParams.offerSdp!!)
                    log("Creating ANSWER...")
                    pcClient.createAnswer()
                }
                signalingParams.iceCandidates.forEach { pcClient.addRemoteIceCandidate(it) }
            }
        }


        private fun disconnectWithErrorMessage(errorMessage: String) {
            log("Connection error: $errorMessage")
            disconnect()
        }

        private fun log(msg: String) {
            Log.d(tag, msg)
        }

        private fun disconnect() {
            handleIntent(Disconnect)
        }

        private inner class SignalingEvents : AppRtcClient.SignalingEvents {
            override fun onRemoteDescription(sdp: SessionDescription) {
                handleAction(RemoteDescriptionReady(sdp))
            }

            override fun onRemoteIceCandidate(candidate: IceCandidate) {
                pcClient.addRemoteIceCandidate(candidate)
            }

            override fun onRemoteIceCandidatesRemoved(candidates: List<IceCandidate>) {
                pcClient.removeRemoteIceCandidates(candidates)
            }

            override fun onChannelClose() {
                disconnect()
            }

            override fun onChannelError(description: String?) {
                disconnectWithErrorMessage("$description")
            }
        }

        private inner class PeerConnectionEvents :
            PeerConnectionClient.Companion.PeerConnectionEvents {

            override fun onLocalDescription(sdp: SessionDescription) {
                handleAction(LocalDescriptionReady(sdp))
            }

            override fun onIceCandidate(candidate: IceCandidate) {
                handleAction(IceCandidateReady(candidate))
            }

            override fun onIceCandidatesRemoved(candidates: List<IceCandidate>) {
                handleAction(IceCandidatesRemoved(candidates))
            }

            override fun onIceConnected() {
                log("ICE connected")
            }

            override fun onIceDisconnected() {
                log("ICE disconnected")
            }

            override fun onConnected() {
                log("DTLS connected")
            }

            override fun onDisconnected() {
                log("DTLS disconnected")
                disconnect()
            }

            override fun onPeerConnectionClosed() {
                log("PeerConnection closed")
            }

            override fun onPeerConnectionStatsReady(reports: Array<RtcStatsReport>) {}

            override fun onPeerConnectionError(description: String?) {
                disconnectWithErrorMessage("$description")
            }

            override fun onLocalVideoTrack(track: VideoTrack) {
                dispatch(LocalVideoTrack(track))
            }

            override fun onRemoteVideoTrack(track: VideoTrack) {
                dispatch(RemoteVideoTrack(track))
            }
        }
    }
}