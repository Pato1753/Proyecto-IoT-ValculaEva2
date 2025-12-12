// Archivo: HorariosViewModel.kt
package com.example.proyecto_iot

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class HorariosViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val collectionRef = db.collection("horarios_global")

    // Lista de horarios para la UI
    val horariosList = mutableStateOf<List<HorarioEntry>>(emptyList())
    val error = mutableStateOf<String?>(null)

    init {
        // Escuchar en tiempo real
        collectionRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                error.value = "Error al cargar horarios: ${e.message}"
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val lista = mutableListOf<HorarioEntry>()
                for (document in snapshot.documents) {
                    // Convertimos el documento a objeto
                    val horario = document.toObject(HorarioEntry::class.java)
                    if (horario != null) {
                        // ¡TRUCO! Guardamos el ID del documento dentro del objeto
                        // para poder usarlo después al editar/borrar
                        horario.id = document.id
                        lista.add(horario)
                    }
                }
                horariosList.value = lista
                error.value = null
            }
        }
    }

    // --- FUNCIONES (Añadir y Editar) ---

    fun agregarHorario(hora: String) {
        val nuevoHorario = mapOf(
            "hora" to hora,
            "activo" to true
        )
        collectionRef.add(nuevoHorario)
    }

    fun toggleHorario(horario: HorarioEntry) {
        // Actualizamos solo el campo 'activo' del documento específico
        collectionRef.document(horario.id).update("activo", !horario.activo)
    }

    fun eliminarHorario(id: String) {
        collectionRef.document(id).delete()
    }
}