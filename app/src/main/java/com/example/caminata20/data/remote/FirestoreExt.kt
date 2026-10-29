package com.example.caminata20.data.remote

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Escucha en tiempo real (incluye datos de la caché offline). */
fun DocumentReference.observe(): Flow<DocumentSnapshot?> = callbackFlow {
    val reg = addSnapshotListener(MetadataChanges.EXCLUDE) { snap, err ->
        if (err != null) trySend(null) else trySend(snap)
    }
    awaitClose { reg.remove() }
}

fun Query.observe(): Flow<QuerySnapshot?> = callbackFlow {
    val reg = addSnapshotListener(MetadataChanges.EXCLUDE) { snap, err ->
        if (err != null) trySend(null) else trySend(snap)
    }
    awaitClose { reg.remove() }
}
