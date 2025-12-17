package com.example.proyecto_iot

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Warning // ¡IMPORTANTE PARA LA ALERTA!
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToHistorial: () -> Unit,
    onNavigateToNotificaciones: () -> Unit,
    onNavigateToHorarios: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val estaAbierta = viewModel.estaAbierta
    val modoAutomatico = viewModel.modoAutomatico
    val humedad = viewModel.humedad
    val nivelEstanque = viewModel.nivelEstanque
    val duracion = viewModel.duracionApertura

    // 1. LEEMOS EL ESTADO DE CONEXIÓN (¡ESTO FALTABA!)
    val arduinoConectado = viewModel.arduinoConectado

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Control") },
                actions = {
                    IconButton(onClick = onNavigateToHorarios) {
                        Icon(Icons.Default.Schedule, contentDescription = "Horarios")
                    }
                    IconButton(onClick = onNavigateToHistorial) {
                        Icon(Icons.Default.History, contentDescription = "Historial")
                    }
                    IconButton(onClick = onNavigateToNotificaciones) {
                        Icon(Icons.Default.Notifications, contentDescription = "Alertas")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) { // <--- ¡AQUÍ ESTABA EL ERROR! Faltaba cerrar el paréntesis y abrir la llave

            // --- 2. ALERTA DE DESCONEXIÓN ---
            if (!arduinoConectado) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFCDD2)), // Rojo claro
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Arduino Desconectado", fontWeight = FontWeight.Bold, color = Color.Red)
                            Text("Funcionando en modo automático local.", fontSize = 12.sp, color = Color.Black)
                        }
                    }
                }
            }

            // --- Tarjetas de Información ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoCard(
                    titulo = "Humedad",
                    valor = "$humedad%",
                    icono = Icons.Default.WaterDrop,
                    colorIcono = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f)
                )
                InfoCard(
                    titulo = "Estanque",
                    valor = nivelEstanque,
                    icono = Icons.Default.WaterDrop,
                    colorIcono = if (nivelEstanque == "Vacío") Color.Red else Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
            }

            Divider()

            // --- Control de Válvula ---
            Text("Control de Válvula", style = MaterialTheme.typography.titleMedium)

            EstadoValvulaCard(estaAbierta = estaAbierta)

            BotonPower(
                estaAbierta = estaAbierta,
                habilitado = !modoAutomatico && arduinoConectado, // Se deshabilita si no hay conexión
                onClick = { viewModel.toggleValvulaManual() }
            )

            // Configuración de Tiempo
            if (!modoAutomatico && arduinoConectado) {
                OutlinedTextField(
                    value = duracion,
                    onValueChange = { viewModel.actualizarDuracion(it) },
                    label = { Text("Duración (segundos)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(0.6f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- Toggle Modo ---
            ModoToggle(
                esAutomatico = modoAutomatico,
                onModoChange = { index -> viewModel.setModo(index) }
            )
        }
    }
}

// --- Componentes Reutilizables ---

@Composable
fun InfoCard(titulo: String, valor: String, icono: androidx.compose.ui.graphics.vector.ImageVector, colorIcono: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icono, contentDescription = null, tint = colorIcono)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = valor, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(text = titulo, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
    }
}

@Composable
fun EstadoValvulaCard(estaAbierta: Boolean) {
    val colorFondo = if (estaAbierta) Color(0xFFE6F4EA) else Color(0xFFFFF0F0)
    val colorTexto = if (estaAbierta) Color(0xFF4CAF50) else Color(0xFFF44336)
    val texto = if (estaAbierta) "Abierta" else "Cerrada"
    val descripcion = if (estaAbierta) "Riego activo" else "Riego detenido"

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = colorFondo)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Estado: $texto", color = colorTexto, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(descripcion, color = Color.DarkGray, fontSize = 14.sp)
        }
    }
}

@Composable
fun BotonPower(estaAbierta: Boolean, habilitado: Boolean, onClick: () -> Unit) {
    val colorBoton = if (estaAbierta) Color(0xFFF44336) else Color(0xFF4CAF50)
    Button(
        onClick = onClick,
        enabled = habilitado,
        modifier = Modifier.size(140.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = colorBoton, disabledContainerColor = Color.Gray)
    ) {
        Icon(Icons.Default.PowerSettingsNew, contentDescription = "Power", modifier = Modifier.size(60.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModoToggle(esAutomatico: Boolean, onModoChange: (Int) -> Unit) {
    val opciones = listOf("Manual", "Automático")
    val selectedIndex = if (esAutomatico) 1 else 0
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        opciones.forEachIndexed { index, label ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = opciones.size),
                onClick = { onModoChange(index) },
                selected = (index == selectedIndex)
            ) { Text(label) }
        }
    }
}