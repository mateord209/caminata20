import java.util.Properties

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.caminata20"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.caminata20"
        minSdk = 24
        targetSdk = 34
        // En CI: VERSION_CODE = número de ejecución y VERSION_NAME = tag v* (sin la "v").
        versionCode = (System.getenv("VERSION_CODE") ?: "1").toInt()
        versionName = System.getenv("VERSION_NAME") ?: "1.0"
        // RIESGO: todo lo que va en BuildConfig queda dentro del APK (ver docs/SEGURIDAD_GROQ.md).
        // Si GROQ_PROXY_URL está definido, la app NO usa GROQ_API_KEY y llama al proxy con el ID token de Firebase.
        buildConfigField("String", "GROQ_API_KEY", "\"${localProperties.getProperty("GROQ_API_KEY", "")}\"")
        buildConfigField("String", "GROQ_PROXY_URL", "\"${localProperties.getProperty("GROQ_PROXY_URL", "")}\"")

    }

    // Firma de la release: se lee de variables de entorno (CI) o de local.properties / -P (local).
    // Nunca se commitean credenciales. Si faltan, la release se firma con la clave debug
    // y el CI lo marca en el nombre del archivo (…-debugsigned.apk).
    fun signingValue(key: String): String? =
        System.getenv(key)?.takeIf { it.isNotBlank() }
            ?: (project.findProperty(key) as String?)?.takeIf { it.isNotBlank() }
            ?: localProperties.getProperty(key)?.takeIf { it.isNotBlank() }

    val keystorePath = signingValue("KEYSTORE_FILE")
    val hasReleaseKeystore = keystorePath != null && file(keystorePath).exists() &&
        listOf("KEYSTORE_PASSWORD", "KEY_ALIAS", "KEY_PASSWORD").all { signingValue(it) != null }

    signingConfigs {
        if (hasReleaseKeystore) {
            create("release") {
                storeFile = file(keystorePath!!)
                storePassword = signingValue("KEYSTORE_PASSWORD")
                keyAlias = signingValue("KEY_ALIAS")
                keyPassword = signingValue("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            signingConfig = if (hasReleaseKeystore) signingConfigs.getByName("release")
                            else signingConfigs.getByName("debug")
            // R8 desactivado por defecto: la app no se ha probado minificada en dispositivo.
            // Para probarlo: ENABLE_R8=true (env) o -PENABLE_R8=true. Reglas en proguard-rules.pro.
            val r8 = signingValue("ENABLE_R8") == "true"
            isMinifyEnabled = r8
            isShrinkResources = r8
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:34.19.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Jetpack Compose (BreakeeTheme + componentes ui/components)
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.datastore.preferences)

    // Inyección de dependencias (Hilt) y coroutines sobre Tasks de Firebase
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.kotlinx.coroutines.play.services)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.core.ktx)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.org.json)
}
// Hilt 2.57 (último compatible con AGP 8.x) trae kotlin-metadata-jvm 2.2; Kotlin 2.4 necesita uno más nuevo.
configurations.configureEach {
    resolutionStrategy.force("org.jetbrains.kotlin:kotlin-metadata-jvm:${libs.versions.kotlin.get()}")
}
