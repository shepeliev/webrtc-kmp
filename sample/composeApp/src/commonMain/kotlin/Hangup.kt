import com.shepeliev.webrtckmp.PeerConnection

fun hangup(peerConnections: Pair<PeerConnection, PeerConnection>?) {
    val (pc1, pc2) = peerConnections ?: return
    pc1.getTransceivers().forEach { pc1.removeTrack(it.sender) }
    pc1.close()
    pc2.close()
}
