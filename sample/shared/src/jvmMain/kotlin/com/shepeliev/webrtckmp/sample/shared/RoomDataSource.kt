package com.shepeliev.webrtckmp.sample.shared

import android.app.Application
import com.google.firebase.FirebasePlatform
import com.google.firebase.firestore.DocumentChange
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.initialize
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.Date
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual class RoomDataSource actual constructor() {

    private val firebaseApp by lazy {
        FirebasePlatform.initializeFirebasePlatform(object : FirebasePlatform() {
            val storage = mutableMapOf<String, String>()
            override fun store(key: String, value: String) = storage.set(key, value)
            override fun retrieve(key: String) = storage[key]
            override fun clear(key: String) {
                storage.remove(key)
            }

            override fun log(msg: String) = println(msg)
        })

        Firebase.initialize(
            Application(),
            FirebaseOptions(
                projectId = "app-rtc-kmp",
                applicationId = "1:216132728347:web:f10a385863ec2d43872abe",
                apiKey = "AIzaSyDa0FDyeGNZZcKKBXnALeJSqfUxSNKut4w",
                authDomain = "app-rtc-kmp.firebaseapp.com",
                storageBucket = "app-rtc-kmp.appspot.com",
                gcmSenderId = "216132728347",
            ),
        )
    }

    private val firestore by lazy {
        Firebase.firestore(firebaseApp).apply {
            setSettings(persistenceEnabled = false)
        }.android
    }
    private val roomsRef by lazy { firestore.collection("rooms") }

    actual suspend fun createRoom(): String {
        return roomsRef.document().id
    }

    actual suspend fun insertOffer(roomId: String, description: SessionDescription) {
        roomsRef.document(roomId).set(
            mapOf(
                "offer" to description.sdp,
                "expireAt" to getExpireAtTime()
            )
        ).await()
    }

    actual suspend fun insertAnswer(roomId: String, description: SessionDescription) {
        roomsRef.document(roomId).update(mapOf("answer" to description.sdp)).await()
    }

    actual suspend fun insertIceCandidate(roomId: String, peerName: String, candidate: IceCandidate) {
        roomsRef.document(roomId)
            .collection(peerName)
            .add(
                mapOf(
                    "candidate" to candidate.candidate,
                    "sdpMLineIndex" to candidate.sdpMLineIndex,
                    "sdpMid" to candidate.sdpMid,
                    "expireAt" to getExpireAtTime(),
                )
            )
            .await()
    }

    private fun getExpireAtTime(): Date {
        val expireAt = System.currentTimeMillis() + FIRESTORE_DOCUMENT_TTL_SECONDS * 1000
        return Date(expireAt)
    }

    actual suspend fun getOffer(roomId: String): SessionDescription? {
        val snapshot = roomsRef.document(roomId).get().await()
        val offerSdp = snapshot.takeIf { it.exists() }?.getString("offer")
        return offerSdp?.let { SessionDescription(SessionDescriptionType.Offer, it) }
    }

    actual suspend fun getAnswer(roomId: String): SessionDescription = suspendCancellableCoroutine { cont ->
        val registration = roomsRef.document(roomId).addSnapshotListener { value, error ->
            val answer = value?.data?.get("answer") as? String
            when {
                answer != null -> cont.resume(SessionDescription(SessionDescriptionType.Answer, answer))
                error != null -> cont.resumeWithException(error)
            }
        }

        cont.invokeOnCancellation { registration.remove() }
    }

    actual fun observeIceCandidates(roomId: String, peerName: String): Flow<IceCandidate> = callbackFlow {
        val registration = roomsRef.document(roomId).collection(peerName).addSnapshotListener { value, error ->
            if (error != null) {
                channel.close(error)
            }

            val dc = value?.documentChanges ?: return@addSnapshotListener

            dc.filter { it.type == DocumentChange.Type.ADDED }
                .map {
                    IceCandidate(
                        sdpMid = it.document.data["sdpMid"] as String,
                        sdpMLineIndex = (it.document.data["sdpMLineIndex"] as Long).toInt(),
                        candidate = it.document.data["candidate"] as String,
                    )
                }
                .forEach { trySendBlocking(it) }
        }

        awaitClose { registration.remove() }
    }
}
