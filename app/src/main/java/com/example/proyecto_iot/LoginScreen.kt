package com.example.proyecto_iot

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

// 1. Esta es la pantalla de UI.
//    Recibe como parámetro la acción de "navegar" cuando el login es exitoso
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit, // Función para navegar a la app principal
    onNavigateToRegister: () -> Unit
) {
    // 2. Obtenemos una instancia del "Cerebro" (ViewModel)
    val viewModel: LoginViewModel = viewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Bienvenido de Nuevo", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(32.dp))

        // 3. Conectamos el TextField al ViewModel
        TextField(
            value = viewModel.email, // Lee el estado del ViewModel
            onValueChange = { viewModel.onEmailChange(it) }, // Avisa al ViewModel del cambio
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Conectamos el TextField de contraseña al ViewModel
        TextField(
            value = viewModel.password, // Lee el estado del ViewModel
            onValueChange = { viewModel.onPasswordChange(it) }, // Avisa al ViewModel del cambio
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(), // Oculta la contraseña
            modifier = Modifier.fillMaxWidth()
        )

        // 5. Mostramos el error si existe
        viewModel.loginError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 6. Conectamos el Botón al ViewModel
        Button(
            onClick = {
                // Le dice al "Cerebro" que inicie la lógica de login
                viewModel.onLoginClick(onLoginSuccess = onLoginSuccess)
            },
            enabled = !viewModel.isLoading, // Deshabilita el botón si está cargando
            modifier = Modifier.fillMaxWidth()
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Iniciar Sesión")
            }
        }
        // ... (justo después del botón de "Iniciar Sesión")

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToRegister) { // Llama a la nueva función
            Text("¿No tienes una cuenta? Regístrate")
        }
    }
}