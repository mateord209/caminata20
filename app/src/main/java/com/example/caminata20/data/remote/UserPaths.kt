package com.example.caminata20.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Estructura en Cloud Firestore (todo cuelga del uid; ver firestore.rules):
 *  users/{uid}                         perfil + meta diaria
 *  users/{uid}/settings/preferences    tipo/duración de pausa, recordatorio
 *  users/{uid}/pauseHistory/{id}       historial de pausas
 *  users/{uid}/walks/{id}              caminatas
 *  users/{uid}/aiRoutines/{id}         rutinas de IA completadas
 */
@Singleton
class UserPaths @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
) {
    val uidFlow: Flow<String?> = callbackFlow {
        val l = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.uid) }
        auth.addAuthStateListener(l)
        awaitClose { auth.removeAuthStateListener(l) }
    }.distinctUntilChanged()

    fun uid(): String? = auth.currentUser?.uid

    fun userDoc(uid: String): DocumentReference = db.collection(USERS).document(uid)
    fun preferences(uid: String): DocumentReference = userDoc(uid).collection("settings").document("preferences")
    fun pauseHistory(uid: String): CollectionReference = userDoc(uid).collection("pauseHistory")
    fun walks(uid: String): CollectionReference = userDoc(uid).collection("walks")
    fun aiRoutines(uid: String): CollectionReference = userDoc(uid).collection("aiRoutines")

    /** Emite [whenSignedOut] sin sesión; con sesión delega en [block] (se reinicia al cambiar de usuario). */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun <T> perUser(whenSignedOut: T, block: (String) -> Flow<T>): Flow<T> =
        uidFlow.flatMapLatest { uid -> if (uid == null) flowOf(whenSignedOut) else block(uid) }

    companion object {
        const val USERS = "users"
    }
}
