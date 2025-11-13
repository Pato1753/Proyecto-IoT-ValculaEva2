// Archivo: build.gradle.kts (app)
// Reemplaza todo tu archivo con esto

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.proyecto_iot"
    compileSdk = 36 // CORREGIDO: 'release(36)' no era una sintaxis válida.

    defaultConfig {
        applicationId = "com.example.proyecto_iot"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // AÑADIDO: Necesario para que Compose use drawables vectoriales
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        // CAMBIADO: A la versión estándar 1.8, más compatible
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        // CAMBIADO: A la versión estándar 1.8
        jvmTarget = "1.8"
    }

    // --- ¡¡AQUÍ ESTÁ LA PARTE IMPORTANTE QUE FALTABA!! ---

    // AÑADIDO: Habilitar Jetpack Compose
    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // --- LIBRERÍAS DE COMPOSE (LAS QUE FALTABAN) ---
    implementation(libs.androidx.core.ktx) // Esta ya la tenías
    implementation(libs.androidx.lifecycle.runtime.ktx) // Necesaria para el ciclo de vida
    implementation(libs.androidx.activity.compose) // Para que 'ComponentActivity' entienda Compose
    implementation(platform(libs.androidx.compose.bom)) // BOM para Compose, maneja versiones
    implementation(libs.androidx.ui) // Componentes base de UI
    implementation(libs.androidx.ui.graphics) // Gráficos
    implementation(libs.androidx.ui.tooling.preview) // Para el @Preview
    implementation(libs.androidx.material3) // ¡Material Design 3! (Button, TextField, etc.)

    // --- LIBRERÍAS DE FIREBASE ---
    implementation(platform("com.google.firebase:firebase-bom:34.5.0")) // Esto ya lo tenías
    implementation("com.google.firebase:firebase-analytics") // Ya lo tenías

    // AÑADIDO: ViewModel para Compose (ya lo tenías)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")

    // ¡¡AÑADIDAS!! Las que realmente necesitas para el login y la BD
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // --- LIBRERÍAS ANTIGUAS (ELIMINADAS) ---
    // implementation(libs.androidx.appcompat) // Esto es para XML
    implementation(libs.material) // Esto es Material 2 para XML
    // implementation(libs.androidx.constraintlayout) // Esto es para XML
    implementation(libs.androidx.navigation.compose)

    // --- LIBRERÍAS DE TEST ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.material.icons.extended)
}