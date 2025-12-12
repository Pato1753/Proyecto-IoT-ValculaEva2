// Archivo: HistorialScreen.kt
package com.example.proyecto_iot

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday // Icono Calendario
import androidx.compose.material.icons.filled.Refresh // Icono Recargar
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    onNavigateBack: () -> Unit,
    viewModel: HistorialViewModel = viewModel()
) {
    // 1. Observamos los datos del ViewModel
    val historial = viewModel.historialList.value
    val error = viewModel.error.value

    // 2. Preparativos para el Calendario (DatePicker)
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // Esta función abre el selector de fecha nativo de Android
    fun mostrarCalendario() {
        DatePickerDialog(
            context,
            { _: DatePicker, anio: Int, mes: Int, dia: Int ->
                // Cuando el usuario selecciona una fecha y da "OK":
                // Llamamos a la función de filtrado del ViewModel
                viewModel.filtrarPorFecha(anio, mes, dia)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial (Últimos 10)") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // Botón para RECARGAR (volver a ver los últimos 10 sin filtros)
                    IconButton(onClick = { viewModel.cargarHistorialInicial() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recargar")
                    }
                    // Botón para FILTRAR (abrir calendario)
                    IconButton(onClick = { mostrarCalendario() }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Filtrar por Fecha")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Manejo de errores
            if (error != null) {
                Text(
                    text = "Error: $error",
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            }
            // Manejo de lista vacía
            else if (historial.isEmpty()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No se encontraron registros para esta fecha.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.cargarHistorialInicial() }) {
                        Text("Ver Todos")
                    }
                }
            }
            // Lista de datos
            else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(historial) { entrada ->
                        HistorialItemRow(entry = entrada)
                        Divider()
                    }
                }
            }
        }
    }
}

/**
 * Componente para dibujar una fila del historial
 */
@Composable
fun HistorialItemRow(entry: HistorialEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(40.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.descripcion,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = formatTimestamp(entry.timestamp),
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

/**
 * Función para dar formato legible a la fecha
 */
@Composable
private fun formatTimestamp(timestamp: Timestamp?): String {
    if (timestamp == null) return "Obteniendo fecha..."
    // Formato con año incluido para que se vea claro el filtro
    val sdf = remember { SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.getDefault()) }
    return sdf.format(timestamp.toDate())
}