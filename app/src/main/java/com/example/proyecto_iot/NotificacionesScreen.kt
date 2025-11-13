// Archivo: NotificacionesScreen.kt
package com.example.proyecto_iot

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesScreen(
    onNavigateBack: () -> Unit, // Función para volver atrás
    viewModel: NotificacionesViewModel = viewModel() // 1. Obtenemos el "Cerebro"
) {
    // 2. Obtenemos la lista y el error
    val notificaciones = viewModel.notificacionesList.value
    val error = viewModel.error.value

    Scaffold(
        // 3. Barra superior
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        // 4. Contenido principal
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // 5. Manejo de estados (Error, Vacío, Datos)
            if (error != null) {
                Text(
                    text = "Error: $error",
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            } else if (notificaciones.isEmpty()) {
                Text("No hay notificaciones.")
            } else {
                // 6. La lista de notificaciones
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(notificaciones) { notificacion ->
                        NotificacionItemRow(entry = notificacion)
                        Divider()
                    }
                }
            }
        }
    }
}

/**
 * UI para UNA SOLA FILA de la lista de notificaciones
 */
@Composable
fun NotificacionItemRow(entry: NotificacionEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono (basado en el mockup)
        Icon(
            imageVector = Icons.Default.Notifications, // TODO: Cambiar icono según tipo
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(40.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Columna para el título y la descripción
        Column(
            modifier = Modifier.weight(1f) // Ocupa el resto del espacio
        ) {
            Text(
                text = entry.Titulo, // Campo 'titulo'
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = entry.Descripcion, // Campo 'descripcion'
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}