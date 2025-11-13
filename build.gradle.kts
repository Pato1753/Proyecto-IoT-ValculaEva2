// Archivo: <project>/build.gradle.kts
// (Este es el archivo de tu captura, el de nivel de PROYECTO)
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false

    // Añade SÓLO esta línea para Google Services.
    // Esta es la única línea de Firebase que debe estar aquí.
    id("com.google.gms.google-services") version "4.4.4" apply false
}