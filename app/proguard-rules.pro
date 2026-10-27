# Reglas de R8 para Breakee. Hoy isMinifyEnabled = false en release (ver README > CI);
# se dejan listas para activarlo cuando se pruebe la build minificada en un dispositivo.

# Conservar info de líneas para stack traces legibles
-keepattributes SourceFile,LineNumberTable,Signature,*Annotation*,InnerClasses,EnclosingMethod
-renamesourcefileattribute SourceFile

# Modelos de dominio (Firestore se mapea a mano en FirestoreMappers, pero se conservan por seguridad
# si algún día se usa toObject()/@PropertyName)
-keep class com.example.caminata20.domain.model.** { *; }

# Firebase / Firestore / Play Services (las librerías traen sus consumer rules; esto es defensivo)
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
}
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Hilt / Dagger (consumer rules incluidas; se conservan los puntos de entrada generados)
-keep class dagger.hilt.** { *; }
-keep class * extends androidx.lifecycle.ViewModel
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }

# Receivers y servicio declarados en el manifiesto
-keep class com.example.caminata20.**Receiver { *; }
-keep class com.example.caminata20.WalkTrackingService { *; }

# OkHttp / Okio
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# org.json (respuestas de Groq)
-keep class org.json.** { *; }
