package com.example.caminata20.di

import com.example.caminata20.BuildConfig
import com.example.caminata20.data.local.LocalStateRepositoryImpl
import com.example.caminata20.data.remote.GroqConfig
import com.example.caminata20.data.remote.GroqExerciseGenerator
import com.example.caminata20.data.remote.IdTokenProvider
import kotlinx.coroutines.tasks.await
import com.example.caminata20.data.repository.FirebaseAuthRepository
import com.example.caminata20.data.repository.FirestoreAiRoutineRepository
import com.example.caminata20.data.repository.FirestorePauseHistoryRepository
import com.example.caminata20.data.repository.FirestorePreferencesRepository
import com.example.caminata20.data.repository.FirestoreProfileRepository
import com.example.caminata20.data.repository.FirestoreWalkRepository
import com.example.caminata20.data.system.AlarmPauseScheduler
import com.example.caminata20.data.system.AlarmReminderScheduler
import com.example.caminata20.domain.repository.AiExerciseGenerator
import com.example.caminata20.domain.repository.AiRoutineRepository
import com.example.caminata20.domain.repository.AuthRepository
import com.example.caminata20.domain.repository.LocalStateRepository
import com.example.caminata20.domain.repository.PauseHistoryRepository
import com.example.caminata20.domain.repository.PauseNotificationGateway
import com.example.caminata20.domain.repository.PauseScheduler
import com.example.caminata20.domain.repository.PreferencesRepository
import com.example.caminata20.domain.repository.ProfileRepository
import com.example.caminata20.domain.repository.ReminderScheduler
import com.example.caminata20.domain.repository.WalkRepository
import com.example.caminata20.domain.usecase.Clock
import com.example.caminata20.notifications.PauseNotifier
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.persistentCacheSettings
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    @Provides @Singleton
    fun auth(): FirebaseAuth = FirebaseAuth.getInstance()

    /** Firestore con caché persistente (offline) de 100 MB. */
    @Provides @Singleton
    fun firestore(): FirebaseFirestore = FirebaseFirestore.getInstance().apply {
        firestoreSettings = firestoreSettings {
            setLocalCacheSettings(persistentCacheSettings { setSizeBytes(100L * 1024 * 1024) })
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun okHttp(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).build()

    @Provides @Singleton
    fun groqConfig(): GroqConfig = GroqConfig(
        apiKey = BuildConfig.GROQ_API_KEY,
        endpoint = "https://api.groq.com/openai/v1/chat/completions",
        proxyUrl = BuildConfig.GROQ_PROXY_URL,
    )

    @Provides
    fun idTokens(auth: FirebaseAuth): IdTokenProvider = IdTokenProvider {
        auth.currentUser?.getIdToken(false)?.await()?.token
    }

    @Provides @Named("io")
    fun io(): CoroutineDispatcher = Dispatchers.IO

    @Provides @Singleton @ApplicationScope
    fun appScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    fun clock(): Clock = Clock { System.currentTimeMillis() }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds abstract fun auth(impl: FirebaseAuthRepository): AuthRepository
    @Binds abstract fun profile(impl: FirestoreProfileRepository): ProfileRepository
    @Binds abstract fun prefs(impl: FirestorePreferencesRepository): PreferencesRepository
    @Binds abstract fun history(impl: FirestorePauseHistoryRepository): PauseHistoryRepository
    @Binds abstract fun walks(impl: FirestoreWalkRepository): WalkRepository
    @Binds abstract fun aiRoutines(impl: FirestoreAiRoutineRepository): AiRoutineRepository
    @Binds abstract fun aiGenerator(impl: GroqExerciseGenerator): AiExerciseGenerator
    @Binds abstract fun local(impl: LocalStateRepositoryImpl): LocalStateRepository
    @Binds abstract fun pauseScheduler(impl: AlarmPauseScheduler): PauseScheduler
    @Binds abstract fun reminderScheduler(impl: AlarmReminderScheduler): ReminderScheduler
    @Binds abstract fun pauseNotifications(impl: PauseNotifier): PauseNotificationGateway
}

/** Scope que vive lo mismo que el proceso (trabajo que no debe cancelarse con una pantalla/servicio). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
