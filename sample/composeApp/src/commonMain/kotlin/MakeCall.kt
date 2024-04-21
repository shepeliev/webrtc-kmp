import co.touchlab.kermit.Logger
import com.shepeliev.webrtckmp.IceCandidate
import com.shepeliev.webrtckmp.MediaStream
import com.shepeliev.webrtckmp.MediaStreamTrackKind
import com.shepeliev.webrtckmp.OfferAnswerOptions
import com.shepeliev.webrtckmp.PeerConnection
import com.shepeliev.webrtckmp.SignalingState
import com.shepeliev.webrtckmp.VideoStreamTrack
import com.shepeliev.webrtckmp.onConnectionStateChange
import com.shepeliev.webrtckmp.onIceCandidate
import com.shepeliev.webrtckmp.onIceConnectionStateChange
import com.shepeliev.webrtckmp.onSignalingStateChange
import com.shepeliev.webrtckmp.onTrack
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

suspend fun makeCall(
    peerConnections: Pair<PeerConnection, PeerConnection>,
    localStream: MediaStream,
    setRemoteVideoTrack: (VideoStreamTrack?) -> Unit
): Nothing = coroutineScope {
    val (pc1, pc2) = peerConnections
    localStream.tracks.forEach { pc1.addTrack(it) }
    val pc1IceCandidates = mutableListOf<IceCandidate>()
    val pc2IceCandidates = mutableListOf<IceCandidate>()
    pc1.onIceCandidate
        .onEach { Logger.d { "PC1 onIceCandidate: $it" } }
        .onEach {
            if (pc2.signalingState == SignalingState.HaveRemoteOffer) {
                pc2.addIceCandidate(it)
            } else {
                pc1IceCandidates.add(it)
            }
        }
        .launchIn(this)
    pc2.onIceCandidate
        .onEach { Logger.d { "PC2 onIceCandidate: $it" } }
        .onEach {
            if (pc1.signalingState == SignalingState.HaveRemoteOffer) {
                pc1.addIceCandidate(it)
            } else {
                pc2IceCandidates.add(it)
            }
        }
        .launchIn(this)
    pc1.onSignalingStateChange
        .onEach { signalingState ->
            Logger.d { "PC1 onSignalingStateChange: $signalingState" }
            if (signalingState == SignalingState.HaveRemoteOffer) {
                pc2IceCandidates.forEach { pc1.addIceCandidate(it) }
                pc2IceCandidates.clear()
            }
        }
        .launchIn(this)
    pc2.onSignalingStateChange
        .onEach { signalingState ->
            Logger.d { "PC2 onSignalingStateChange: $signalingState" }
            if (signalingState == SignalingState.HaveRemoteOffer) {
                pc1IceCandidates.forEach { pc2.addIceCandidate(it) }
                pc1IceCandidates.clear()
            }
        }
        .launchIn(this)
    pc1.onIceConnectionStateChange
        .onEach { Logger.d { "PC1 onIceConnectionStateChange: $it" } }
        .launchIn(this)
    pc2.onIceConnectionStateChange
        .onEach { Logger.d { "PC2 onIceConnectionStateChange: $it" } }
        .launchIn(this)
    pc1.onConnectionStateChange
        .onEach { Logger.d { "PC1 onConnectionStateChange: $it" } }
        .launchIn(this)
    pc2.onConnectionStateChange
        .onEach { Logger.d { "PC2 onConnectionStateChange: $it" } }
        .launchIn(this)
    pc1.onTrack
        .onEach { Logger.d { "PC1 onTrack: $it" } }
        .launchIn(this)
    pc2.onTrack
        .onEach { Logger.d { "PC2 onTrack: ${it.track?.kind}" } }
        .filter { it.track?.kind == MediaStreamTrackKind.Video }
        .onEach { setRemoteVideoTrack(it.track as VideoStreamTrack) }
        .launchIn(this)
    val offer = pc1.createOffer(OfferAnswerOptions(offerToReceiveVideo = true, offerToReceiveAudio = true))
    pc1.setLocalDescription(offer)
    pc2.setRemoteDescription(offer)
    val answer = pc2.createAnswer(options = OfferAnswerOptions())
    pc2.setLocalDescription(answer)
    pc1.setRemoteDescription(answer)

    awaitCancellation()
}
