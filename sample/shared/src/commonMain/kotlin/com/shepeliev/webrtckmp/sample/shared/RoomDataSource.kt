package com.shepeliev.webrtckmp.sample.shared

import kotlinx.coroutines.flow.Flow

typealias SessionDescription = com.shepeliev.webrtckmp.SessionDescription
typealias IceCandidate = com.shepeliev.webrtckmp.IceCandidate
typealias SessionDescriptionType = com.shepeliev.webrtckmp.SessionDescriptionType

expect class RoomDataSource() {
    fun createRoom(): String
    suspend fun insertOffer(roomId: String, description: SessionDescription)
    suspend fun insertAnswer(roomId: String, description: SessionDescription)
    suspend fun insertIceCandidate(roomId: String, peerName: String, candidate: IceCandidate)
    suspend fun getOffer(roomId: String): SessionDescription?
    suspend fun getAnswer(roomId: String): SessionDescription
    fun observeIceCandidates(roomId: String, peerName: String): Flow<IceCandidate>
}
