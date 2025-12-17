package com.example.proyecto_iot

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

// Usamos AndroidViewModel para tener acceso al 'context' (necesario para notificaciones)
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    // --- Referencias a Firebase ---
    private val realtimeDb = FirebaseDatabase.getInstance()
    private val firestoreDb = FirebaseFirestore.getInstance()

    // Realtime Database (Control y Sensores)
    private val valvulaRef = realtimeDb.getReference("valvula/estado")
    private val sensoresRef = realtimeDb.getReference("sensores")
    private val modoRef = realtimeDb.getReference("config/modoAutomatico")
    private val latidoRef = realtimeDb.getReference("dispositivo/latido")

    // Firestore (Historial y Notificaciones)
    private val historialRef = firestoreDb.collection("historial_global")
    private val notificacionesRef = firestoreDb.collection("notificaciones_globales")

    // --- Estados para la UI (Jetpack Compose) ---
    var estaAbierta by mutableStateOf(false)
        private set
    var modoAutomatico by mutableStateOf(true)
        private set
    var humedad by mutableStateOf(0)
        private set
    var nivelEstanque by mutableStateOf("Cargando...")
        private set
    var duracionApertura by mutableStateOf("5") // String para el TextField
        private set

    // Estado de Conexión (Heartbeat)
    var arduinoConectado by mutableStateOf(false)
        private set
    private var ultimoTiempoLatido: Long = 0

    // Control de notificaciones para no repetir
    private var nivelAnterior = ""

    init {
        // 1. Escuchar estado de la Válvula
        valvulaRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                estaAbierta = snapshot.getValue(Boolean::class.java) ?: false
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // 2. Escuchar Modo (Manual/Automático)
        modoRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                modoAutomatico = snapshot.getValue(Boolean::class.java) ?: true
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // 3. Escuchar Sensores y CREAR NOTIFICACIONES
        sensoresRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    humedad = snapshot.child("humedad").getValue(Int::class.java) ?: 0
                    val nuevoNivel = snapshot.child("nivel").getValue(String::class.java) ?: "---"

                    // Lógica de Notificación Automática
                    // Si el nivel cambió y es crítico (Bajo o Alto), notificamos
                    if (nuevoNivel != nivelAnterior && nuevoNivel != "---") {
                        if (nuevoNivel == "Bajo" || nuevoNivel == "Alto") {
                            generarAlerta(nuevoNivel)
                        }
                        nivelAnterior = nuevoNivel
                    }
                    nivelEstanque = nuevoNivel
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // 4. Sistema Heartbeat (Detector de desconexión)
        latidoRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    ultimoTiempoLatido = System.currentTimeMillis()
                    arduinoConectado = true
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        iniciarVigilanciaConexion()
    }

    // --- Lógica del Cronómetro de Conexión ---
    private fun iniciarVigilanciaConexion() {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                val tiempoActual = System.currentTimeMillis()
                // Si pasaron más de 15 segundos sin latido, declaramos desconexión
                if (tiempoActual - ultimoTiempoLatido > 15000) {
                    arduinoConectado = false
                }
                handler.postDelayed(this, 5000) // Revisar cada 5 segundos
            }
        }
        handler.post(runnable)
    }

    // --- Funciones de Control ---

    fun toggleValvulaManual() {
        if (!modoAutomatico && arduinoConectado) {
            val nuevaAccion = !estaAbierta
            valvulaRef.setValue(nuevaAccion)
            registrarHistorial(if (nuevaAccion) "Válvula Abierta (Manual)" else "Válvula Cerrada (Manual)")
        }
    }

    fun setModo(index: Int) {
        val esAuto = (index == 1)
        modoRef.setValue(esAuto)
        registrarHistorial(if (esAuto) "Modo Automático Activado" else "Modo Manual Activado")

        // Si pasamos a manual, cerramos la válvula por seguridad
        if (!esAuto) valvulaRef.setValue(false)
    }

    fun actualizarDuracion(nuevaDuracion: String) {
        if (nuevaDuracion.all { it.isDigit() }) {
            duracionApertura = nuevaDuracion
        }
    }

    // --- Funciones de Notificación y Historial ---

    private fun registrarHistorial(accion: String) {
        val registro = hashMapOf(
            "accion" to accion,
            "timestamp" to FieldValue.serverTimestamp(),
            "usuario" to "Admin"
        )
        historialRef.add(registro)
    }

    private fun generarAlerta(nivel: String) {
        val titulo = "Alerta de Humedad"
        val mensaje = if (nivel == "Bajo") "¡Peligro! Humedad crítica (Baja)." else "Atención: Exceso de humedad."

        // 1. Guardar en Firestore (Para verlo en la pantalla de notificaciones)
        val notificacion = hashMapOf(
            "titulo" to titulo,
            "descripcion" to mensaje,
            "timestamp" to FieldValue.serverTimestamp(),
            "leido" to false,
            "tipo" to nivel // Para poner íconos de color después si quieres
        )
        notificacionesRef.add(notificacion)

        // 2. Lanzar Notificación Push en el celular
        mostrarNotificacionPush(titulo, mensaje)
    }

    private fun mostrarNotificacionPush(titulo: String, mensaje: String) {
        val channelId = "sensor_alert_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear canal para Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Alertas Sensores", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert) // Icono por defecto
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}