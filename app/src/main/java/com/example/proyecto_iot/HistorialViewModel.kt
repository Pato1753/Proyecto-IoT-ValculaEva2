package com.example.proyecto_iot

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Calendar
import java.util.Date

class HistorialViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val collectionRef = db.collection("historial_global")

    val historialList = mutableStateOf<List<HistorialEntry>>(emptyList())
    val error = mutableStateOf<String?>(null)

    init {
        cargarHistorialInicial()
    }

    // Carga los últimos 10 registros (Requerimiento 1)
    fun cargarHistorialInicial() {
        collectionRef
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(10) // ¡LÍMITE DE 10!
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    error.value = "Error: ${e.message}"
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    historialList.value = snapshot.toObjects(HistorialEntry::class.java)
                }
            }
    }

    // Filtrar por fecha específica (Requerimiento 2)
    fun filtrarPorFecha(anio: Int, mes: Int, dia: Int) {
        // Crear fecha de inicio (00:00:00)
        val inicio = Calendar.getInstance().apply {
            set(anio, mes, dia, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Crear fecha de fin (23:59:59)
        val fin = Calendar.getInstance().apply {
            set(anio, mes, dia, 23, 59, 59)
        }

        // Consultamos a Firebase
        collectionRef
            .whereGreaterThanOrEqualTo("timestamp", Timestamp(inicio.time))
            .whereLessThanOrEqualTo("timestamp", Timestamp(fin.time))
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get() // Usamos get() porque es una búsqueda puntual, no un listener permanente
            .addOnSuccessListener { snapshot ->
                historialList.value = snapshot.toObjects(HistorialEntry::class.java)
            }
            .addOnFailureListener { e ->
                error.value = "Error al filtrar: ${e.message}"
            }
    }
}