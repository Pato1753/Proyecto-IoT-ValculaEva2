// Archivo: HomeScreen.kt
package com.example.proyecto_iot

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
// 1. Para el error 'Unresolved reference 'CircleShape''
import androidx.compose.material.icons.filled.History
// 2. Para el error 'Unresolved reference 'shape''
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material.icons.filled.Notifications // <-- ¡AÑADE ESTA LÍNEA!
// Esta es la función principal de la pantalla
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToHistorial: () -> Unit,
    onNavigateToNotificaciones: () -> Unit,
    viewModel: HomeViewModel = viewModel() // 1. Obtenemos la instancia del "Cerebro"
) {
    val estaAbierta = viewModel.estaAbierta
    val modoAutomatico = viewModel.modoAutomatico

    Scaffold(
        // 2. La barra superior (como en tu mockup)
        topBar = {
            TopAppBar(
                title = { Text("Control de Válvula") },
                actions = {
                    // Botón de Historial (ya lo tenías)
                    IconButton(onClick = onNavigateToHistorial) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "Ver Historial"
                        )
                    }
                    // ¡NUEVO! Botón de Notificaciones
                    IconButton(onClick = onNavigateToNotificaciones) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Ver Notificaciones"
                        )
                    }
                }
            )
        }
    ) { padding ->
        // 3. Columna principal que centra todo
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp), // Añadimos padding general
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween // Empuja el toggle al fondo
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // 4. El Card de Estado (que cambia de color)
                EstadoValvulaCard(estaAbierta = estaAbierta)

                Spacer(modifier = Modifier.height(64.dp))

                // 5. El Botón de Power (que se deshabilita)
                BotonPower(
                    estaAbierta = estaAbierta,
                    habilitado = !modoAutomatico, // ¡LÓGICA CLAVE! Habilitado solo en Manual
                    onClick = { viewModel.toggleValvulaManual() } // Llama a la función del ViewModel
                )
            }

            // 6. El Toggle Manual/Automático (al fondo)
            ModoToggle(
                esAutomatico = modoAutomatico,
                onModoChange = { index -> viewModel.setModo(index) } // Llama al ViewModel
            )
        }
    }
}

// --- Componentes Reutilizables ---

/**
 * El Card que muestra el estado actual (Abierta/Cerrada)
 */
@Composable
fun EstadoValvulaCard(estaAbierta: Boolean) {
    val colorFondo = if (estaAbierta) Color(0xFFE6F4EA) else Color(0xFFFFF0F0)
    val colorTexto = if (estaAbierta) Color(0xFF4CAF50) else Color(0xFFF44336)
    val texto = if (estaAbierta) "Abierta" else "Cerrada"
    val descripcion = if (estaAbierta) "La válvula está permitiendo el paso de agua." else "La válvula está bloqueando el paso de agua."

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorFondo)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Estado: $texto",
                color = colorTexto,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = descripcion,
                color = Color.DarkGray,
                fontSize = 14.sp
            )
        }
    }
}

/**
 * El botón circular de Power
 */
@Composable
fun BotonPower(estaAbierta: Boolean, habilitado: Boolean, onClick: () -> Unit) {
    val colorBoton = if (estaAbierta) Color(0xFFF44336) else Color(0xFF4CAF50) // Color de "apagar" o "encender"
    val textoBoton = if (estaAbierta) "Presiona para cerrar" else "Presiona para abrir"

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Botón
        Button(
            onClick = onClick,
            enabled = habilitado, // Se deshabilita si 'habilitado' es false
            modifier = Modifier
                .size(180.dp), // Tamaño grande
            shape = CircleShape, // Forma circular
            colors = ButtonDefaults.buttonColors(
                containerColor = colorBoton,
                disabledContainerColor = Color.Gray // Color cuando está deshabilitado
            )
        ) {
            Icon(
                Icons.Default.PowerSettingsNew,
                contentDescription = "Power",
                modifier = Modifier.size(80.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Texto descriptivo
        Text(
            text = if (habilitado) textoBoton else "Modo Automático Activado",
            fontSize = 16.sp,
            color = if (habilitado) Color.Black else Color.Gray
        )
    }
}

/**
 * El Toggle de modo Manual/Automático
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModoToggle(esAutomatico: Boolean, onModoChange: (Int) -> Unit) {
    val opciones = listOf("Manual", "Automático")
    val selectedIndex = if (esAutomatico) 1 else 0

    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        opciones.forEachIndexed { index, label ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = opciones.size
                ),
                onClick = { onModoChange(index) },
                selected = (index == selectedIndex)
            ) {
                Text(text = label)
            }
        }
    }
}

