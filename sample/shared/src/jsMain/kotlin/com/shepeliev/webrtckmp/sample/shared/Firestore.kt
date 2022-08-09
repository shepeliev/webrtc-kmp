@file:JsModule("firebase/firestore")
@file:JsNonModule

package com.shepeliev.webrtckmp.sample.shared

import kotlin.js.Json
import kotlin.js.Promise

external class Firestore

external interface Query

external interface CollectionReference : Query

external interface DocumentReference {
    val id: String
}

external interface DocumentSnapshot {
    val id: String
    fun data(): Json
    fun exists(): Boolean
}

external interface DocumentChange {
    val doc: DocumentSnapshot
    val type: String
}

external interface QuerySnapshot {
    fun docChanges(): Array<DocumentChange>
}

external fun getFirestore(app: FirebaseApp): Firestore

external fun collection(firestore: Firestore, path: String): CollectionReference

external fun collection(reference: CollectionReference, path: String): CollectionReference

external fun doc(reference: CollectionReference, path: String? = definedExternally): DocumentReference

external fun addDoc(reference: CollectionReference, data: Json): Promise<dynamic>

external fun setDoc(reference: DocumentReference, data: Json): Promise<dynamic>

external fun updateDoc(reference: DocumentReference, data: Json): Promise<dynamic>

external fun getDoc(reference: DocumentReference): Promise<DocumentSnapshot>

external fun onSnapshot(query: Query, observer: QuerySnapshotObserver): () -> Unit

external fun onSnapshot(reference: DocumentReference, observer: DocumentSnapshotObserver): () -> Unit
