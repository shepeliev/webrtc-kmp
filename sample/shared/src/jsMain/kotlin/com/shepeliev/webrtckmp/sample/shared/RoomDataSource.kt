package com.shepeliev.webrtckmp.sample.shared

import kotlinx.coroutines.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.js.Date
import kotlin.js.json

actual class RoomDataSource actual constructor() {

    private val firestore by lazy {
        initializeApp(
            json(
                "applicationId" to "1:216132728347:web:f10a385863ec2d43872abe",
                "apiKey" to "AIzaSyDa0FDyeGNZZcKKBXnALeJSqfUxSNKut4w",
                "authDomain" to "app-rtc-kmp.firebaseapp.com",
                "projectId" to "app-rtc-kmp",
                "storageBucket" to "app-rtc-kmp.appspot.com",
                "gcmSenderId" to "216132728347",
            )
        ).let { getFirestore(it) }
    }

    private val roomsRef by lazy { collection(firestore, "rooms") }

    actual fun createRoom(): String {
        return doc(roomsRef).id
    }

    actual suspend fun insertOffer(roomId: String, description: SessionDescription) {
        val docRef = doc(roomsRef, roomId)
        setDoc(
            docRef,
            json(
                "offer" to description.sdp,
                "expireAt" to getExpireAtTime()
            )
        ).await()
    }

    actual suspend fun insertAnswer(roomId: String, description: SessionDescription) {
        val docRef = doc(roomsRef, roomId)
        updateDoc(
            docRef,
            json(
                "answer" to description.sdp,
                "expireAt" to getExpireAtTime()
            )
        ).await()
    }

    actual suspend fun insertIceCandidate(roomId: String, peerName: String, candidate: IceCandidate) {
        val colRef = collection(roomsRef, "$roomId/$peerName")
        addDoc(
            colRef,
            json(
                "candidate" to candidate.candidate,
                "sdpMLineIndex" to candidate.sdpMLineIndex,
                "sdpMid" to candidate.sdpMid,
                "expireAt" to getExpireAtTime(),
            )
        ).await()
    }

    private fun getExpireAtTime(): Date {
        val expireAt = Date().getTime() + FIRESTORE_DOCUMENT_TTL_SECONDS * 1000
        return Date(expireAt)
    }

    actual suspend fun getOffer(roomId: String): SessionDescription? {
        val snapshot = getDoc(doc(roomsRef, roomId)).await()
        val offerSdp = snapshot.takeIf { it.exists() }?.data()?.get("offer") as? String
        return offerSdp?.let { SessionDescription(SessionDescriptionType.Offer, it) }
    }

    actual suspend fun getAnswer(roomId: String): SessionDescription = suspendCancellableCoroutine { cont ->
        val observer = DocumentSnapshotObserver(
            next = { snapshot ->
                val answer = snapshot.data()["answer"] as? String
                answer?.let { cont.resume(SessionDescription(SessionDescriptionType.Answer, it)) }
            },

            error = cont::resumeWithException,

            complete = cont::cancel
        )
        val unsubscribe = onSnapshot(doc(roomsRef, roomId), observer)
        cont.invokeOnCancellation { unsubscribe() }
    }

    actual fun observeIceCandidates(roomId: String, peerName: String): Flow<IceCandidate> = callbackFlow {
        val observer = QuerySnapshotObserver(
            next = { querySnapshot ->

                querySnapshot.docChanges()
                    .filter { it.type == "added" }
                    .map {
                        IceCandidate(
                            sdpMid = it.doc.data()["sdpMid"] as String,
                            sdpMLineIndex = it.doc.data()["sdpMLineIndex"] as Int,
                            candidate = it.doc.data()["candidate"] as String,
                        )
                    }
                    .forEach { trySend(it) }
            },

            error = channel::close,

            complete = channel::close
        )

        val unsubscribe = onSnapshot(collection(roomsRef, "$roomId/$peerName"), observer)

        awaitClose { unsubscribe() }
    }
}
