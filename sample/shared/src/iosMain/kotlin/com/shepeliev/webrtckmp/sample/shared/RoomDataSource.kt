package com.shepeliev.webrtckmp.sample.shared

import cocoapods.FirebaseCore.FIRApp
import cocoapods.FirebaseFirestore.FIRDocumentChange
import cocoapods.FirebaseFirestore.FIRDocumentChangeType
import cocoapods.FirebaseFirestore.FIRDocumentSnapshot
import cocoapods.FirebaseFirestore.FIRFirestore
import cocoapods.FirebaseFirestore.FIRQuerySnapshot
import com.shepeliev.webrtckmp.SessionDescription
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSDate
import platform.Foundation.NSError
import platform.Foundation.dateByAddingTimeInterval
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual class RoomDataSource actual constructor() {

    private val roomsRef by lazy {
        FIRApp.configure()
        FIRFirestore.firestore().collectionWithPath("rooms")
    }

    actual suspend fun createRoom(): String {
        return roomsRef.documentWithAutoID().documentID
    }

    actual suspend fun insertOffer(roomId: String, description: SessionDescription) =
        suspendCancellableCoroutine<Unit> { cont ->
            roomsRef.documentWithPath(roomId)
                .setData(
                    documentData = mapOf(
                        "offer" to description.sdp,
                        "expireAt" to getExpireAtTime()
                    ),
                    merge = true,
                    completion = { error ->
                        error
                            ?.let { cont.resumeWithException(Exception(it.description)) }
                            ?: cont.resume(Unit)
                    }
                )
        }

    actual suspend fun insertAnswer(roomId: String, description: SessionDescription) =
        suspendCancellableCoroutine<Unit> { cont ->
            roomsRef.documentWithPath(roomId)
                .updateData(
                    fields = mapOf(
                        "answer" to description.sdp,
                        "expireAt" to getExpireAtTime()
                    ),
                    completion = { error ->
                        error
                            ?.let { cont.resumeWithException(Exception(it.description)) }
                            ?: cont.resume(Unit)
                    }
                )
        }

    actual suspend fun insertIceCandidate(roomId: String, peerName: String, candidate: IceCandidate) =
        suspendCancellableCoroutine<Unit> { cont ->
            roomsRef
                .documentWithPath(roomId)
                .collectionWithPath(peerName)
                .documentWithAutoID()
                .setData(
                    documentData = mapOf(
                        "candidate" to candidate.candidate,
                        "sdpMLineIndex" to candidate.sdpMLineIndex,
                        "sdpMid" to candidate.sdpMid,
                        "expireAt" to getExpireAtTime(),
                    ),
                    completion = { error ->
                        error
                            ?.let { cont.resumeWithException(Exception(it.description)) }
                            ?: cont.resume(Unit)
                    }
                )
        }

    actual suspend fun getOffer(roomId: String): SessionDescription? = suspendCancellableCoroutine { cont ->
        roomsRef.documentWithPath(roomId).getDocumentWithCompletion { snapshot, error ->
            error?.let { cont.resumeWithException(Exception(it.description)) }
            val offerSdp = snapshot
                ?.takeIf { it.exists }
                ?.data()
                ?.get("offer") as? String
            cont.resume(offerSdp?.let { SessionDescription(SessionDescriptionType.Offer, it) })
        }
    }

    actual suspend fun getAnswer(roomId: String): SessionDescription = suspendCancellableCoroutine { cont ->
        val snapshotListener = { snapshot: FIRDocumentSnapshot?, error: NSError? ->
            if (cont.isActive) {
                error?.let { cont.resumeWithException(Exception(it.description)) }
                val answer = snapshot?.data()?.get("answer") as? String
                answer?.let { cont.resume(SessionDescription(SessionDescriptionType.Answer, it)) }
            }
            Unit
        }

        val registration = roomsRef
            .documentWithPath(roomId)
            .addSnapshotListener(snapshotListener)

        cont.invokeOnCancellation { registration.remove() }
    }

    actual fun observeIceCandidates(roomId: String, peerName: String): Flow<IceCandidate> = callbackFlow {
        val listener = { snapshot: FIRQuerySnapshot?, error: NSError? ->
            error?.let { channel.close(Exception(it.description)) }

            snapshot?.documentChanges
                ?.map { it as FIRDocumentChange }
                ?.filter { it.type == FIRDocumentChangeType.FIRDocumentChangeTypeAdded }
                ?.map {
                    IceCandidate(
                        sdpMid = it.document.data()["sdpMid"] as String,
                        sdpMLineIndex = (it.document.data()["sdpMLineIndex"] as Long).toInt(),
                        candidate = it.document.data()["candidate"] as String,
                    )
                }
                ?.forEach { trySend(it) }

            Unit
        }

        val registration = roomsRef
            .documentWithPath(roomId)
            .collectionWithPath(peerName)
            .addSnapshotListener(listener)

        awaitClose { registration.remove() }
    }

    private fun getExpireAtTime(): NSDate {
        return NSDate().dateByAddingTimeInterval(FIRESTORE_DOCUMENT_TTL_SECONDS.toDouble())
    }
}
