package com.example.proyecto_iot

import com.google.firebase.Timestamp


data class NotificacionEntry(
    val titulo: String = "",
    val descripcion: String = "",
    val timestamp: Timestamp? = null
) {
    constructor() : this("", "", null)
}