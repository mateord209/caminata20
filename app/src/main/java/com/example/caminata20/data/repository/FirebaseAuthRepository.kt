package com.example.caminata20.data.repository

import com.example.caminata20.domain.model.AuthUser
import com.example.caminata20.domain.model.Outcome
import com.example.caminata20.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
) : AuthRepository {

    override val currentUser: Flow<AuthUser?> = callbackFlow {
        val l = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.toDomain()) }
        auth.addAuthStateListener(l)
        awaitClose { auth.removeAuthStateListener(l) }
    }

    override fun currentUserOrNull(): AuthUser? = auth.currentUser?.toDomain()

    override suspend fun signInWithEmail(email: String, password: String): Outcome<AuthUser> = runAuth({
        when (it) {
            is FirebaseAuthInvalidUserException -> "Usuario no encontrado"
            is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrectos"
            else -> "Error al iniciar sesión, intentá de nuevo"
        }
    }) { auth.signInWithEmailAndPassword(email, password).await().user }

    override suspend fun signInWithGoogle(idToken: String): Outcome<AuthUser> = runAuth({ "Error al iniciar con Google" }) {
        auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await().user
    }

    override suspend fun register(name: String, email: String, password: String): Outcome<AuthUser> = runAuth({
        when (it) {
            is FirebaseAuthUserCollisionException -> "Ya existe una cuenta con ese correo"
            is FirebaseAuthWeakPasswordException -> "La contraseña es muy débil"
            is FirebaseAuthInvalidCredentialsException -> "El correo ingresado no es válido"
            else -> "Error al crear la cuenta, intentá de nuevo"
        }
    }) {
        val user = auth.createUserWithEmailAndPassword(email, password).await().user
        user?.updateProfile(userProfileChangeRequest { displayName = name })?.await()
        auth.currentUser
    }

    override suspend fun sendPasswordReset(email: String): Outcome<Unit> = try {
        auth.setLanguageCode("es")
        auth.sendPasswordResetEmail(email).await()
        Outcome.Success(Unit)
    } catch (e: Exception) {
        Outcome.Failure("Error: ${e.message}")
    }

    override suspend fun updateDisplayName(name: String) {
        runCatching { auth.currentUser?.updateProfile(userProfileChangeRequest { displayName = name })?.await() }
    }

    override fun signOut() = auth.signOut()

    private suspend fun runAuth(message: (Exception) -> String, block: suspend () -> FirebaseUser?): Outcome<AuthUser> = try {
        val user = block() ?: throw IllegalStateException("Sin usuario")
        Outcome.Success(user.toDomain())
    } catch (e: Exception) {
        Outcome.Failure(message(e))
    }

    private fun FirebaseUser.toDomain() = AuthUser(uid, displayName, email)
}
