package com.example.caminata20.data.repository

import com.example.caminata20.domain.model.AuthUser
import com.example.caminata20.domain.model.Outcome
import com.example.caminata20.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val errors: AuthErrorMapper,
) : AuthRepository {

    override val currentUser: Flow<AuthUser?> = callbackFlow {
        val l = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.toDomain()) }
        auth.addAuthStateListener(l)
        awaitClose { auth.removeAuthStateListener(l) }
    }

    override fun currentUserOrNull(): AuthUser? = auth.currentUser?.toDomain()

    override suspend fun signInWithEmail(email: String, password: String): Outcome<AuthUser> = runAuth {
        auth.signInWithEmailAndPassword(email, password).await().user
    }

    override suspend fun signInWithGoogle(idToken: String): Outcome<AuthUser> = runAuth {
        auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await().user
    }

    override suspend fun register(name: String, email: String, password: String): Outcome<AuthUser> = runAuth {
        val user = auth.createUserWithEmailAndPassword(email, password).await().user
            ?: throw IllegalStateException("Sin usuario")
        try {
            user.updateProfile(userProfileChangeRequest { displayName = name }).await()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // El nombre se reintenta con ensureProfile; no se revierte la cuenta creada.
        }
        user
    }

    override suspend fun sendPasswordReset(email: String): Outcome<Unit> = try {
        auth.setLanguageCode("es")
        auth.sendPasswordResetEmail(email).await()
        Outcome.Success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Outcome.Failure(errors.map(e).userMessage)
    }

    override suspend fun updateDisplayName(name: String) {
        try {
            auth.currentUser?.updateProfile(userProfileChangeRequest { displayName = name })?.await()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // El nombre local ya se guardó; Firestore lo reintenta en el próximo guardado.
        }
    }

    override fun signOut() = auth.signOut()

    private suspend fun runAuth(block: suspend () -> FirebaseUser?): Outcome<AuthUser> = try {
        val user = block() ?: throw IllegalStateException("Sin usuario")
        Outcome.Success(user.toDomain())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Outcome.Failure(errors.map(e).userMessage)
    }

    private fun FirebaseUser.toDomain() = AuthUser(uid, displayName, email)
}
