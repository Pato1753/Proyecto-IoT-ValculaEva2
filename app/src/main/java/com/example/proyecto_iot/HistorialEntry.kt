package com.example.proyecto_iot

import com.google.firebase.Timestamp


data class HistorialEntry(
    val descripcion: String = "",
    val timestamp: Timestamp? = null
) {

    constructor() : this("", null)
}