package com.example.proyecto_iot

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class HorariosViewModel : ViewModel() {

    private val firestoreDb = FirebaseFirestore.getInstance()
    private val firestoreRef = firestoreDb.collection("horarios_global")

    // Referencia a Realtime Database (Para hablar con el Arduino)
    private val realtimeDb = FirebaseDatabase.getInstance()
    private val arduinoHoraRef = realtimeDb.getReference("config/horaProgramada")
    private val arduinoDuracionRef = realtimeDb.getReference("config/duracionProgramada")

    // Lista de horarios para la UI
    val horariosList = mutableStateOf<List<HorarioEntry>>(emptyList())
    val error = mutableStateOf<String?>(null)

    init {
        // Escuchar cambios en Firestore para actualizar la lista visual
        firestoreRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                error.value = "Error: ${e.message}"
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val lista = mutableListOf<HorarioEntry>()
                for (document in snapshot.documents) {
                    val horario = document.toObject(HorarioEntry::class.java)
                    if (horario != null) {
                        horario.id = document.id
                        lista.add(horario)
                    }
                }
                horariosList.value = lista
            }
        }
    }

    // --- FUNCIONES (UI + Arduino Sync) ---

    fun agregarHorario(hora: String) {
        // 1. Guardar en Firestore (Visual)
        val nuevoHorario = mapOf(
            "hora" to hora,
            "activo" to true
        )
        firestoreRef.add(nuevoHorario)

        // 2. ENVIAR AL ARDUINO (Para que funcione de verdad)
        // Nota: Enviamos el último horario añadido como el "vigente" para el Arduino
        actualizarArduino(hora, 10) // Por defecto 10 segundos de riego
    }

    fun toggleHorario(horario: HorarioEntry) {
        val nuevoEstado = !horario.activo

        // 1. Actualizar Firestore
        firestoreRef.document(horario.id).update("activo", nuevoEstado)

        // 2. Actualizar Arduino
        if (nuevoEstado) {
            // Si lo activamos, le decimos al Arduino que esta es la hora nueva
            actualizarArduino(horario.hora, 10)
        } else {
            // Si lo desactivamos, podríamos borrar la hora del Arduino o dejarla
            // Para este ejemplo, si desactivas el riego, enviamos hora vacía al Arduino
            // para que no riegue por error.
            actualizarArduino("", 0)
        }
    }

    fun eliminarHorario(id: String) {
        firestoreRef.document(id).delete()
        // Opcional: Si borras el horario activo, limpiar Arduino
    }

    // Función auxiliar para escribir en Realtime Database
    private fun actualizarArduino(hora: String, duracion: Int) {
        arduinoHoraRef.setValue(hora)
        arduinoDuracionRef.setValue(duracion)
    }
}