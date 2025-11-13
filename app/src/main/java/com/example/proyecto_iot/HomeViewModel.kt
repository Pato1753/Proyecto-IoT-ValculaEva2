// Archivo: HomeViewModel.kt
package com.example.proyecto_iot

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue // ¡NUEVO! Importamos el Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class HomeViewModel : ViewModel() {

    // 1. Conexiones (Sin cambios)
    private val db = FirebaseFirestore.getInstance()
    private val docRef = db.collection("valvula").document("estado_actual")
    // ¡NUEVO! Referencia a la nueva colección de historial
    private val historialRef = db.collection("historial_global")

    // 2. Estados (Sin cambios)
    var estaAbierta by mutableStateOf(false)
        private set
    var modoAutomatico by mutableStateOf(true)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    // 3. Listener en tiempo real (Sin cambios)
    init {
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("HomeViewModel", "Error al escuchar", e)
                error = "Error de conexión: ${e.message}"
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                Log.d("HomeViewModel", "Datos recibidos: ${snapshot.data}")
                estaAbierta = snapshot.getBoolean("esta_abierta") ?: false
                modoAutomatico = snapshot.getBoolean("modo_automatico") ?: true
                error = null
            } else {
                Log.d("HomeViewModel", "No hay datos (documento no existe)")
                error = "No se encontró el documento de estado."
            }
        }
    }

    // 4. ¡NUEVO! Función para guardar en el historial
    /**
     * Crea un nuevo documento en la colección 'historial_global'.
     */
    private fun registrarAccionEnHistorial(descripcion: String) {
        // Creamos un "mapa" (un objeto) con los datos del registro
        val registro = mapOf(
            "descripcion" to descripcion,
            "timestamp" to FieldValue.serverTimestamp() // ¡Usa la hora del servidor!
        )

        // Añadimos el documento a la colección.
        // Firebase le pondrá un ID automático.
        historialRef.add(registro)
            .addOnSuccessListener {
                Log.d("HomeViewModel", "Acción registrada: $descripcion")
            }
            .addOnFailureListener { e ->
                Log.w("HomeViewModel", "Error al registrar acción", e)
                // Opcional: podríamos mostrar este error en la UI
            }
    }


    // 5. Funciones de escritura (¡MODIFICADAS!)

    fun setModo(index: Int) {
        val esAutomatico = (index == 1)
        docRef.update("modo_automatico", esAutomatico)
            .addOnFailureListener { e ->
                error = "Error al cambiar modo: ${e.message}"
            }

        // ¡NUEVO! Registramos la acción en el historial
        val desc = if (esAutomatico) "Modo Automático activado" else "Modo Manual activado"
        registrarAccionEnHistorial(desc)
    }

    fun toggleValvulaManual() {
        if (modoAutomatico) {
            Log.d("HomeViewModel", "Bloqueado: No se puede cambiar la válvula en modo automático.")
            return
        }

        docRef.update("esta_abierta", !estaAbierta)
            .addOnFailureListener { e ->
                error = "Error al cambiar estado: ${e.message}"
            }

        // ¡NUEVO! Registramos la acción en el historial
        // Nota: 'estaAbierta' tiene el valor *anterior* al clic.
        // Por eso, si 'estaAbierta' es 'false', la acción es "Abrir".
        val desc = if (!estaAbierta) "Válvula Abierta" else "Válvula Cerrada"
        registrarAccionEnHistorial(desc)
    }
}