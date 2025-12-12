package com.example.proyecto_iot

import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel // Cambiamos a AndroidViewModel para tener acceso al Contexto
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

// Heredamos de AndroidViewModel para poder usar 'getApplication()'
class NotificacionesViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FirebaseFirestore.getInstance()
    val notificacionesList = mutableStateOf<List<NotificacionEntry>>(emptyList())
    val error = mutableStateOf<String?>(null)

    // Variable para saber si es la primera vez que cargamos los datos
    private var esCargaInicial = true

    init {
        db.collection("notificaciones_globales")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    error.value = "Error: ${e.message}"
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    // 1. Actualizamos la lista para la UI (igual que antes)
                    notificacionesList.value = snapshot.toObjects(NotificacionEntry::class.java)

                    // 2. ¡NUEVO! Detectamos si hay CAMBIOS (nuevos documentos)
                    // Solo notificamos si NO es la carga inicial
                    if (!esCargaInicial) {
                        for (cambio in snapshot.documentChanges) {
                            if (cambio.type == DocumentChange.Type.ADDED) {
                                // ¡Llegó una nueva notificación!
                                val nuevaNoti = cambio.document.toObject(NotificacionEntry::class.java)
                                lanzarNotificacion(nuevaNoti.titulo, nuevaNoti.descripcion)
                            }
                        }
                    }
                    esCargaInicial = false // Ya terminó la primera carga
                }
            }
    }

    private fun lanzarNotificacion(titulo: String, mensaje: String) {
        val context = getApplication<Application>().applicationContext

        // Verificamos permiso (Obligatorio en Android modernos)
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Si no hay permiso, no podemos notificar.
            // (En una app real, aquí pedirías el permiso al usuario)
            return
        }

        val builder = NotificationCompat.Builder(context, "canal_iot_alertas")
            .setSmallIcon(android.R.drawable.ic_dialog_alert) // Ícono por defecto de Android
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // ¡Alta prioridad!
            .setAutoCancel(true)

        // Usamos un ID único (System.currentTimeMillis) para que no se reemplacen entre sí
        NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), builder.build())
    }
}