// Archivo: HorariosScreen.kt
package com.example.proyecto_iot

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorariosScreen(
    onNavigateBack: () -> Unit,
    viewModel: HorariosViewModel = viewModel()
) {
    val horarios = viewModel.horariosList.value
    val context = LocalContext.current // Necesario para mostrar el reloj

    // Función para mostrar el Reloj de Android
    fun mostrarReloj() {
        val calendario = Calendar.getInstance()
        val horaActual = calendario.get(Calendar.HOUR_OF_DAY)
        val minutoActual = calendario.get(Calendar.MINUTE)

        TimePickerDialog(context, { _, hora, minuto ->
            // Formateamos la hora (ej: "08:05")
            val horaFormateada = String.format("%02d:%02d", hora, minuto)
            viewModel.agregarHorario(horaFormateada)
        }, horaActual, minutoActual, true).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Programar Riego") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        // Botón flotante (+) para añadir horario
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarReloj() }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (horarios.isEmpty()) {
                Text(
                    text = "No hay horarios programados.",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    items(horarios) { horario ->
                        HorarioRow(
                            horario = horario,
                            onToggle = { viewModel.toggleHorario(horario) },
                            onDelete = { viewModel.eliminarHorario(horario.id) }
                        )
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
fun HorarioRow(horario: HorarioEntry, onToggle: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = horario.hora,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (horario.activo) "Activo" else "Inactivo",
                fontSize = 14.sp,
                color = if (horario.activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Interruptor On/Off
            Switch(
                checked = horario.activo,
                onCheckedChange = { onToggle() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            // Botón Borrar
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}