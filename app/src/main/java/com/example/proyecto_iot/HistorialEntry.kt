// Archivo: HistorialEntry.kt
package com.example.proyecto_iot

// Importamos la clase "Timestamp" de Firebase
import com.google.firebase.Timestamp

/**
 * Este es el "molde" para cada registro en nuestro historial.
 * Los nombres (descripcion, timestamp) deben coincidir EXACTAMENTE
 * con los nombres de los campos que guardamos en Firebase.
 */
data class HistorialEntry(
    val descripcion: String = "", // El texto (ej. "Válvula Abierta")
    val timestamp: Timestamp? = null // La hora del servidor
) {
    // Constructor vacío OBLIGATORIO para que Firebase pueda
    // convertir automáticamente los documentos en esta clase.
    constructor() : this("", null)
}