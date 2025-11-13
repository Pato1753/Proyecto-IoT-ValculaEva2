// Archivo: NotificacionEntry.kt
package com.example.proyecto_iot

import com.google.firebase.Timestamp

/**
 * Este es el "molde" para cada notificación.
 * Los nombres (titulo, descripcion, timestamp) deben coincidir EXACTAMENTE
 * con los campos que creaste en Firebase.
 */
data class NotificacionEntry(
    val Titulo: String = "",
    val Descripcion: String = "",
    val timestamp: Timestamp? = null
) {
    // Constructor vacío OBLIGATORIO para que Firebase pueda
    // convertir automáticamente los documentos en esta clase.
    constructor() : this("", "", null)
}