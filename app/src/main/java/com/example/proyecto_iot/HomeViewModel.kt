package com.example.proyecto_iot

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class HomeViewModel : ViewModel() {

    // 1. Usamos Realtime Database para el control rápido (Arduino)
    private val realtimeDb = FirebaseDatabase.getInstance()
    private val controlRef = realtimeDb.getReference("valvula/estado") // Boolean (true/false)
    private val sensoresRef = realtimeDb.getReference("sensores")

    // 2. Mantenemos Firestore SOLO para el Historial (es mejor para guardar registros)
    private val firestoreDb = FirebaseFirestore.getInstance()
    private val historialRef = firestoreDb.collection("historial_global") // Ojo: Ajuste abajo

    // --- ESTADOS ---
    var estaAbierta by mutableStateOf(false)
        private set
    var modoAutomatico by mutableStateOf(true) // Esto lo manejaremos local o en RTDB si quieres
        private set
    var humedad by mutableStateOf(0)
        private set
    var nivelEstanque by mutableStateOf("---")
        private set
    var duracionApertura by mutableStateOf("10")
        private set

    var error by mutableStateOf<String?>(null)
        private set

    init {
        // A. Escuchar estado de la Válvula (Arduino <-> App)
        controlRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Si el valor existe, actualizamos. Si no, asumimos false.
                estaAbierta = snapshot.getValue(Boolean::class.java) ?: false
            }
            override fun onCancelled(error: DatabaseError) {
                // Manejo de error silencioso
            }
        })

        // B. Escuchar Sensores (Arduino -> App)
        sensoresRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Leemos humedad y nivel que envía el Arduino
                humedad = snapshot.child("humedad").getValue(Int::class.java) ?: 0
                nivelEstanque = snapshot.child("nivel").getValue(String::class.java) ?: "---"
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    // --- FUNCIONES DE ESCRITURA ---

    fun setModo(index: Int) {
        modoAutomatico = (index == 1)
        registrarAccionEnHistorial(if (modoAutomatico) "Modo Automático" else "Modo Manual")
    }

    fun actualizarDuracion(nuevaDuracion: String) {
        if (nuevaDuracion.all { it.isDigit() } && nuevaDuracion.isNotEmpty()) {
            duracionApertura = nuevaDuracion
        }
    }

    fun toggleValvulaManual() {
        if (modoAutomatico) return

        // ¡AQUÍ ESTÁ LA CLAVE! Escribimos en Realtime Database
        // El Arduino detectará este cambio instantáneamente.
        controlRef.setValue(!estaAbierta)
            .addOnFailureListener { e ->
                error = "Error: ${e.message}"
            }
            .addOnSuccessListener {
                val desc = if (!estaAbierta) "Válvula Abierta" else "Válvula Cerrada"
                registrarAccionEnHistorial(desc)
            }
    }

    // Guardamos el historial en Firestore (es más barato y ordenado para listas largas)
    private fun registrarAccionEnHistorial(descripcion: String) {
        val registro = mapOf(
            "descripcion" to descripcion,
            "timestamp" to FieldValue.serverTimestamp()
        )
        // Nota: Usamos la colección de Firestore como antes
        firestoreDb.collection("historial_global").add(registro)
    }
}