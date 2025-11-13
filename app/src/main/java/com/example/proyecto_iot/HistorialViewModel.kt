// Archivo: HistorialViewModel.kt
package com.example.proyecto_iot

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query // ¡Importante para ordenar!

class HistorialViewModel : ViewModel() {

    // 1. Referencia a la base de datos
    private val db = FirebaseFirestore.getInstance()

    // 2. Estado que la UI observará
    //    Esta será la lista de entradas del historial
    val historialList = mutableStateOf<List<HistorialEntry>>(emptyList())

    // 3. Estado para errores
    val error = mutableStateOf<String?>(null)

    init {
        // 4. Conectarnos a Firebase en cuanto se cree el ViewModel
        db.collection("historial_global")
            // ¡LÓGICA CLAVE! Ordenar por "timestamp" en orden descendente
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                // Si hay un error
                if (e != null) {
                    Log.w("HistorialViewModel", "Error al escuchar", e)
                    error.value = "Error al cargar historial: ${e.message}"
                    return@addSnapshotListener
                }

                // Si snapshot no es nulo (¡hay datos!)
                if (snapshot != null) {
                    // Convertimos los documentos de Firebase
                    // a una lista de nuestra data class 'HistorialEntry'
                    historialList.value = snapshot.toObjects(HistorialEntry::class.java)
                    error.value = null
                }
            }
    }
}