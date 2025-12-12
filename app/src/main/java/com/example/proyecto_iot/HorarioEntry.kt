// Archivo: HorarioEntry.kt
package com.example.proyecto_iot

data class HorarioEntry(
    var id: String = "", // El ID del documento en Firebase (para poder editarlo)
    val hora: String = "", // Ej: "08:30"
    val activo: Boolean = true // Si el horario está encendido o apagado
) {
    // Constructor vacío requerido por Firebase
    constructor() : this("", "", true)
}