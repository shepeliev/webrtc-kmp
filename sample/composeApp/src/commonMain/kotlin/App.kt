import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import co.touchlab.kermit.platformLogWriter
import com.shepeliev.webrtckmp.IceCandidate
import com.shepeliev.webrtckmp.MediaDevices
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
import com.shepeliev.webrtckmp.videoTracks
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    LaunchedEffect(Unit) {
        Logger.setLogWriters(platformLogWriter())
    }

    MaterialTheme {
        val scope = rememberCoroutineScope()
        val (localStream, setLocalStream) = remember { mutableStateOf<MediaStream?>(null) }
        val (remoteVideoTrack, setRemoteVideoTrack) = remember { mutableStateOf<VideoStreamTrack?>(null) }
        val (peerConnections, setPeerConnections) = remember {
            mutableStateOf<Pair<PeerConnection, PeerConnection>?>(null)
        }

        LaunchedEffect(localStream, peerConnections) {
            if (peerConnections == null || localStream == null) return@LaunchedEffect
            call(peerConnections, localStream, setRemoteVideoTrack)
        }

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            val localVideoTrack = localStream?.videoTracks?.firstOrNull()

            localVideoTrack?.let { Video(track = it, modifier = Modifier.weight(1f)) }
                ?: Box(modifier = Modifier.weight(1f).background(color = Color.Black).fillMaxWidth())

            remoteVideoTrack?.let { Video(track = it, modifier = Modifier.weight(1f)) }
                ?: Box(modifier = Modifier.weight(1f).background(color = Color.Black).fillMaxWidth())

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (localStream != null) {
                    SwitchCameraButton(
                        onClick = {
                            scope.launch { localStream.videoTracks.firstOrNull()?.switchCamera() }
                        }
                    )
                }

                when {
                    localStream == null -> {
                        StartButton(onClick = {
                            scope.launch {
                                val stream = MediaDevices.getUserMedia(audio = true, video = true)
                                setLocalStream(stream)
                            }
                        })
                    }

                    peerConnections != null -> {
                        HangupButton(onClick = {
                            hangup(peerConnections, setPeerConnections, setRemoteVideoTrack)
                        })
                    }

                    else -> {
                        CallButton(
                            onClick = { setPeerConnections(Pair(PeerConnection(), PeerConnection())) },
                        )
                    }
                }

                if (localStream != null) {
                    EndButton(
                        onClick = {
                            hangup(peerConnections, setPeerConnections, setRemoteVideoTrack)
                            localStream.release()
                            setLocalStream(null)
                        }
                    )
                }
            }
        }
    }
}

private suspend fun call(
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

private fun hangup(
    peerConnections: Pair<PeerConnection, PeerConnection>?,
    setPeerConnections: (Pair<PeerConnection, PeerConnection>?) -> Unit,
    setRemoteVideoTrack: (VideoStreamTrack?) -> Unit
) {
    val (pc1, pc2) = peerConnections ?: return
    pc1.getTransceivers().forEach { pc1.removeTrack(it.sender) }
    pc1.close()
    pc2.close()
    setPeerConnections(null)
    setRemoteVideoTrack(null)
}

@Composable
fun CallButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick, modifier = modifier) {
        Text("Call")
    }
}

@Composable
fun HangupButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick, modifier = modifier) {
        Text("Hangup")
    }
}

@Composable
fun SwitchCameraButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick = onClick, modifier = modifier) {
        Text("Switch Camera")
    }
}

@Composable
fun EndButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick = onClick, modifier = modifier) {
        Text("End")
    }
}
