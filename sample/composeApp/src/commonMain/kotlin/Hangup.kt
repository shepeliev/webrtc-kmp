import com.shepeliev.webrtckmp.PeerConnection
import com.shepeliev.webrtckmp.VideoStreamTrack

fun hangup(
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
