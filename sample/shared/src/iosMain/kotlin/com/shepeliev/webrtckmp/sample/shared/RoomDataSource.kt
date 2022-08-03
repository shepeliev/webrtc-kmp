package com.shepeliev.webrtckmp.sample.shared

import com.shepeliev.webrtckmp.IceCandidate
import com.shepeliev.webrtckmp.SessionDescription
import kotlinx.coroutines.flow.Flow

actual class RoomDataSource actual constructor() {

    actual fun createRoom(): String {
        TODO("not implemented")
    }

    actual suspend fun insertOffer(roomId: String, description: SessionDescription) {
        TODO("not implemented")
    }

    actual suspend fun insertAnswer(roomId: String, description: SessionDescription) {
        TODO("not implemented")
    }

    actual suspend fun insertIceCandidate(roomId: String, peerName: String, candidate: IceCandidate) {
        TODO("not implemented")
    }

    actual suspend fun getOffer(roomId: String): SessionDescription? {
        TODO("not implemented")
    }

    actual suspend fun getAnswer(roomId: String): SessionDescription {
        TODO("not implemented")
    }

    actual fun observeIceCandidates(roomId: String, peerName: String): Flow<IceCandidate> {
        TODO("not implemented")
    }
}
