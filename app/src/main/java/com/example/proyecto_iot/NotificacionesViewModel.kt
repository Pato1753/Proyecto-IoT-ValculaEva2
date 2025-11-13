// Archivo: NotificacionesViewModel.kt
package com.example.proyecto_iot

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query // ¡Importante para ordenar!

class NotificacionesViewModel : ViewModel() {

    // 1. Referencia a la base de datos
    private val db = FirebaseFirestore.getInstance()

    // 2. Estado que la UI observará
    val notificacionesList = mutableStateOf<List<NotificacionEntry>>(emptyList())

    // 3. Estado para errores
    val error = mutableStateOf<String?>(null)

    init {
        // 4. Conectarnos a Firebase en cuanto se cree el ViewModel
        db.collection("notificaciones_globales") // <-- La nueva colección
            .orderBy("timestamp", Query.Direction.DESCENDING) // Ordenar por fecha
            .addSnapshotListener { snapshot, e ->
                // Si hay un error
                if (e != null) {
                    Log.w("NotificacionesVM", "Error al escuchar", e)
                    error.value = "Error al cargar notificaciones: ${e.message}"
                    return@addSnapshotListener
                }

                // Si snapshot no es nulo (¡hay datos!)
                if (snapshot != null) {
                    // Convertimos los documentos de Firebase
                    // a una lista de nuestra data class 'NotificacionEntry'
                    notificacionesList.value = snapshot.toObjects(NotificacionEntry::class.java)
                    error.value = null
                }
            }
    }
}