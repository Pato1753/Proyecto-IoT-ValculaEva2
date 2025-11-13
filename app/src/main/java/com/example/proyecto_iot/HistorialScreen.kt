// Archivo: HistorialScreen.kt
package com.example.proyecto_iot

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // ¡Importante para LazyColumn!
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
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
import java.text.SimpleDateFormat // ¡Importante para formatear la fecha!
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    onNavigateBack: () -> Unit, // Función para volver a la pantalla anterior
    viewModel: HistorialViewModel = viewModel() // 1. Obtenemos el "Cerebro"
) {
    // 2. Obtenemos la lista y el error del ViewModel
    val historial = viewModel.historialList.value
    val error = viewModel.error.value

    Scaffold(
        // 3. Barra superior con título y botón de "volver"
        topBar = {
            TopAppBar(
                title = { Text("Historial de Actividad") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { // Llama a la navegación
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
            // 5. Si hay un error, lo mostramos
            if (error != null) {
                Text(
                    text = "Error: $error",
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            }
            // 6. Si la lista está vacía (y no hay error), mostramos un mensaje
            else if (historial.isEmpty()) {
                Text("Aún no hay actividad registrada.")
            }
            // 7. Si hay datos, mostramos la lista (¡el LazyColumn!)
            else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // "items" es la función que dibuja la lista
                    items(historial) { entrada ->
                        // Por cada 'entrada' en la lista, dibuja una 'HistorialItemRow'
                        HistorialItemRow(entry = entrada)
                        Divider() // Una línea divisoria entre cada ítem
                    }
                }
            }
        }
    }
}

/**
 * Esta es la UI para UNA SOLA FILA de la lista.
 * Se basa en el mockup.
 */
@Composable
fun HistorialItemRow(entry: HistorialEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // (Icono - Opcional, pero estaba en tu mockup)
        Icon(
            imageVector = Icons.Default.Warning, // TODO: Cambiar icono según el tipo
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(40.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Columna para la descripción y la hora
        Column(
            modifier = Modifier.weight(1f) // Ocupa el resto del espacio
        ) {
            Text(
                text = entry.descripcion,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = formatTimestamp(entry.timestamp), // Usamos la función de formato
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

/**
 * Función "ayudante" para convertir el Timestamp de Firebase
 * en un texto legible (ej. "13 Nov, 1:30 AM")
 */
@Composable
private fun formatTimestamp(timestamp: Timestamp?): String {
    if (timestamp == null) return "Obteniendo fecha..."

    // Usamos 'remember' para no recalcular esto en cada redibujo
    val sdf = remember { SimpleDateFormat("dd MMM, h:mm a", Locale.getDefault()) }
    return sdf.format(timestamp.toDate())
}